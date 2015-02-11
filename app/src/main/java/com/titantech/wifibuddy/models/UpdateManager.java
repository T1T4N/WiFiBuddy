package com.titantech.wifibuddy.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.titantech.wifibuddy.service.IntentFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by Robert on 11.02.2015.
 */
public class UpdateManager {
    private static final String TAG = "UPDATE_MANAGER";
    private static UpdateManager instance = null;
    private SharedPreferences mPreferences;
    private Date mLastUpdate;
    private Context mApplicationContext;

    private UpdateManager(Context applicationContext){
        mApplicationContext = applicationContext;
        mPreferences = applicationContext.getSharedPreferences(Constants.PREFS_NAME, Activity.MODE_PRIVATE);
        String lastUpdate = mPreferences.getString(Constants.PREFS_KEY_LAST_UPDATE, null);
        if(lastUpdate != null){
            try {
                mLastUpdate = Utils.datetime_format.parse(lastUpdate);
            } catch (ParseException e) {
                mLastUpdate = new Date();
            }
        } else {
            mLastUpdate = Utils.long_ago;
        }
        readPreviousUpdates();
    }
    public static void setupInstance(Context applicationContext){
        if(instance == null) {
            synchronized(UpdateManager.class) {
                instance = new UpdateManager(applicationContext);
            }
        }
    }
    public static UpdateManager getInstance() throws RuntimeException{
        if(instance == null) {
            throw new RuntimeException("The UpdateManager is not initialized");
        }
        return instance;
    }

    private void readPreviousUpdates(){
        try {
            FileInputStream fis = mApplicationContext.openFileInput(Constants.FILENAME_UPDATES);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while((line = br.readLine()) != null){
                //TODO: Read saved updates in there was no internet connection to send them to the server
            }
            br.close();
            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "No previous queued updates found");
            try {
                FileOutputStream fos = mApplicationContext.openFileOutput(Constants.FILENAME_UPDATES, Context.MODE_PRIVATE);
                fos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean shouldUpdate(String timeNow) throws ParseException{
        return shouldUpdate(Utils.datetime_format.parse(timeNow));
    }
    public  boolean shouldUpdate(Date timeNow){
        long diff = timeNow.getTime() - mLastUpdate.getTime();
        long diffMinutes = diff / (60 * 1000) % 60;
        return diffMinutes > 60;
    }

    public void queueInsert(AccessPoint ap){

    }
    public void queueUpdate(Uri updateUri, AccessPoint changedAp) {
        // Update local database
        mApplicationContext.getContentResolver().update(updateUri, changedAp.toContentValues(), null, null);
        if(Utils.isInternetAvailable(mApplicationContext)){
            // Perform server update right away
            // TODO: Register Broadcast Receiver to get result status
            mApplicationContext.startService(IntentFactory.putItem(mApplicationContext, changedAp));
        } else {
            // TODO: Save update locally if it cannot be sent to the server immediately
        }
    }
}
