package com.titantech.wifibuddy.models;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.titantech.wifibuddy.service.IntentFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public class UpdateManager {
    private static final String TAG = "UPDATE_MANAGER";
    private static UpdateManager instance = null;
    private SharedPreferences mPreferences;
    private Date mLastUpdate;
    private Context mApplicationContext;
    private Set<UpdateTask> updateTasks;
    private IntentFilter mFilter;

    private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();

            int updateResult = intent.getIntExtra(Constants.SERVICE_UPDATE_RESULT_STATUS, 0);
            UpdateTask task = intent.getParcelableExtra(Constants.SERVICE_UPDATE_RESULT_TASK);
            if (updateResult != Constants.SERVICE_RESULT_UNREACHABLE &&
                    updateResult != Constants.SERVICE_RESULT_UNAUTHORIZED) {
                if(task.isWritten && task.lineIndex >= 0){
                    // TODO: Remove task from the local file
                }
                else {
                    updateTasks.remove(task);
                }
            }
        }
    };

    private UpdateManager(Context applicationContext) {
        mApplicationContext = applicationContext;
        mPreferences = applicationContext.getSharedPreferences(Constants.PREFS_NAME, Activity.MODE_PRIVATE);
        String lastUpdate = mPreferences.getString(Constants.PREFS_KEY_LAST_UPDATE, null);
        if (lastUpdate != null) {
            try {
                mLastUpdate = Utils.parseDate(lastUpdate);
            } catch (ParseException e) {
                mLastUpdate = new Date();
            }
        } else {
            mLastUpdate = Utils.long_ago;
        }
        updateTasks = new TreeSet<UpdateTask>();
        mFilter = new IntentFilter();
        mFilter.addAction(Constants.SERVICE_UPDATE_COMPLETED);

        readPreviousUpdates();
        updateDatabase();
        processTasks();
    }

    private void writeUpdateTime(Date lastUpdate){
        SharedPreferences.Editor editor
                = mApplicationContext.getSharedPreferences(Constants.PREFS_NAME, Activity.MODE_PRIVATE).edit();
        editor.putString(Constants.PREFS_KEY_LAST_UPDATE, Utils.formatDate(lastUpdate));
        editor.apply();
    }

    public void updateDatabase() {
        if(shouldUpdate()){
            Toast.makeText(mApplicationContext, "Updating database", Toast.LENGTH_SHORT).show();

            Intent intent = IntentFactory.getPrivateItems(mApplicationContext);
            mApplicationContext.startService(intent);
            intent = IntentFactory.getPublicItems(mApplicationContext);
            mApplicationContext.startService(intent);

            mLastUpdate = new Date();
            writeUpdateTime(mLastUpdate);
        }
    }

    public static void setupInstance(Context applicationContext) {
        if (instance == null) {
            synchronized (UpdateManager.class) {
                instance = new UpdateManager(applicationContext);
            }
        }
    }
    public static UpdateManager getInstance() throws RuntimeException {
        if (instance == null) {
            throw new RuntimeException("The UpdateManager is not initialized");
        }
        return instance;
    }

    private void readPreviousUpdates() {
        try {
            FileInputStream fis = mApplicationContext.openFileInput(Constants.FILENAME_UPDATES);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                if(!line.isEmpty() && !line.trim().equals("")){
                    updateTasks.add(new UpdateTask(idx, line));
                }
                idx++;
            }
            br.close();
            fis.close();
        } catch (IOException e) {
            Log.d(TAG, "No previous queued updates found");
            try {
                FileOutputStream fos = mApplicationContext.openFileOutput(Constants.FILENAME_UPDATES, Context.MODE_PRIVATE);
                fos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean shouldUpdate(Date timeNow) {
        long diff = timeNow.getTime() - mLastUpdate.getTime();
        long diffMinutes = diff / (60 * 1000); // % 60;

        // TODO: Change when debugging complete
        return diffMinutes > 0;
        // return diffMinutes > 60;
    }

    private boolean shouldUpdate() {
        return shouldUpdate(new Date());
    }

    public void processTasks(){
        if(Utils.isInternetAvailable(mApplicationContext)) {
            // Perform server update right away
            if(updateTasks.size() > 0) {
                mApplicationContext.registerReceiver(mStatusReceiver, mFilter);
                Intent intent = IntentFactory.batchActions(mApplicationContext, new ArrayList<UpdateTask>(updateTasks));
                mApplicationContext.startService(intent);
            }
        }
    }
    public void queueInsert(AccessPoint ap) {
        // Update local database
        mApplicationContext.getContentResolver()
                .insert(AccessPoint.getBaseContentUriFromPrivacy(ap.getPrivacyType()), ap.toContentValues());

        UpdateTask updateTask = new UpdateTask(-1, UpdateTask.UpdateType.INSERT, ap, false);
        updateTasks.add(updateTask);
        processTasks();
    }

    public void queueUpdate(AccessPoint changedAp) {
        // Update local database
        mApplicationContext.getContentResolver()
            .update(changedAp.getContentUriFromPrivacy(), changedAp.toContentValues(), null, null);

        UpdateTask updateTask = new UpdateTask(-1, UpdateTask.UpdateType.UPDATE, changedAp, false);
        updateTasks.add(updateTask);
        processTasks();
    }

    public void queueDelete(AccessPoint deletedAp) {
        mApplicationContext.getContentResolver()
            .delete(deletedAp.getContentUriFromPrivacy(), null, null);

        UpdateTask updateTask = new UpdateTask(-1, UpdateTask.UpdateType.DELETE, deletedAp, false);
        updateTasks.add(updateTask);
        processTasks();
    }

    public static class UpdateTask implements Comparable<UpdateTask>, Parcelable{
        public static enum UpdateType {INSERT, UPDATE, DELETE}
        public int lineIndex;
        public UpdateType updateType;
        public int internalId;
        public int privacyType;
        public AccessPoint accessPoint;
        public boolean isWritten;

        public UpdateTask(int lineIdx, String lineContents){
            lineIndex = lineIdx;
            String[] lineOpts = lineContents.split("\\s+");
            if(lineOpts.length != 3){
                throw new RuntimeException("Cannot create UpdateTask due to line format error");
            }
            int type;
            try {
                type = Integer.parseInt(lineOpts[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new RuntimeException("Cannot create UpdateTask due to type format error");
            }
            updateType = UpdateType.values()[type];

            int intId;
            try {
                intId = Integer.parseInt(lineOpts[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new RuntimeException("Cannot create UpdateTask due to internalId format error");
            }
            internalId = intId;

            int privType;
            try {
                privType= Integer.parseInt(lineOpts[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new RuntimeException("Cannot create UpdateTask due to privacyType format error");
            }
            privacyType = privType;

            isWritten = true;
        }

        public UpdateTask(int lineIndex, UpdateType updateType, AccessPoint accessPoint, boolean isWritten){
            this.lineIndex = lineIndex;
            this.updateType = updateType;
            this.internalId = accessPoint.getInternalId();
            this.privacyType = accessPoint.getPrivacyType();
            this.accessPoint = accessPoint;
            this.isWritten = isWritten;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UpdateTask that = (UpdateTask) o;

            if (internalId != that.internalId) return false;
            if (updateType != that.updateType) return false;
            if (privacyType != that.privacyType) return false;
            if (accessPoint != null && !accessPoint.equals(that.accessPoint)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = updateType != null ? updateType.hashCode() : 0;
            result = 31 * result + internalId;
            result = 31 * result + (accessPoint != null ? accessPoint.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(UpdateTask another) {
            if(updateType.ordinal() < another.updateType.ordinal()) {
                return -1;
            }
            else if(updateType.ordinal() > another.updateType.ordinal()) {
                return 1;
            }
            else return 0;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(lineIndex);
            dest.writeInt(updateType.ordinal());
            dest.writeInt(internalId);
            dest.writeInt(privacyType);
            dest.writeParcelable(accessPoint, 0);
            dest.writeInt(isWritten ? 1 : 0);
        }
        public static final Parcelable.Creator<UpdateTask> CREATOR = new Parcelable.Creator<UpdateTask>() {
            @Override
            public UpdateTask createFromParcel(Parcel source) {
                return new UpdateTask(source);
            }

            @Override
            public UpdateTask[] newArray(int size) {
                return new UpdateTask[size];
            }
        };

        public UpdateTask(Parcel parc){
            lineIndex = parc.readInt();
            updateType = UpdateType.values()[parc.readInt()];
            internalId = parc.readInt();
            privacyType = parc.readInt();
            accessPoint = parc.readParcelable(getClass().getClassLoader());
            isWritten = parc.readInt() == 1;
        }
    }
}
