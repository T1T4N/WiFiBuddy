package com.titantech.wifibuddy.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.UpdateManager;
import com.titantech.wifibuddy.models.User;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.network.RestTask;
import com.titantech.wifibuddy.network.ResultListener;
import com.titantech.wifibuddy.network.requests.GetRestRequest;
import com.titantech.wifibuddy.network.requests.PutRestRequest;
import com.titantech.wifibuddy.network.requests.RestRequest;
import com.titantech.wifibuddy.parsers.AccessPointPutParser;
import com.titantech.wifibuddy.parsers.AccessPointsFetchParser;
import com.titantech.wifibuddy.provider.WifiContentProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Robert on 24.01.2015.
 */
public class DataManagerService extends IntentService {
    private static final String TAG = "DATA_MANAGER_SERVICE";

    public DataManagerService() {
        super("datamanager-service");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle data = intent.getExtras();
        if (data != null) {
            final User authUser = data.getParcelable("auth_user");
            int action = data.getInt("action");
            switch (action) {
                case Constants.SERVICE_ACTION_GET_PRIVATE:
                    fetchPrivate(authUser);
                    break;
                case Constants.SERVICE_ACTION_GET_PUBLIC:
                    fetchPublic(authUser);
                    break;
                case Constants.SERVICE_ACTION_BATCH_TASKS:
                    ArrayList<UpdateManager.UpdateTask> tasks = data.getParcelableArrayList("update_item");
                    for(UpdateManager.UpdateTask task : tasks){
                        switch (task.updateType){
                            case INSERT:
                                // TODO: insertAccessPoint
                                break;
                            case UPDATE:
                                putAccessPoint(authUser, task);
                                break;
                            case DELETE:
                                // TODO: deleteAccessPoint
                                break;
                        }
                    }
                    break;
            }
        }
    }

    private void fetchPublic(User authUser) {
        final Context ctx = this;
        String requestUrl = getString(R.string.url_public_aps);
        RestRequest request = new GetRestRequest(requestUrl, authUser.getEmail(), authUser.getPassword());
        RestTask<List<AccessPoint>> fetchTaskPublic = new RestTask<List<AccessPoint>>(
            this, new AccessPointsFetchParser(true), new ResultListener<List<AccessPoint>>() {
            @Override
            public void onDownloadResult(List<AccessPoint> result) {
                if (result != null && !result.equals(Collections.emptyList())) {
                    for (AccessPoint item : result) {
                        getContentResolver().insert(
                            WifiContentProvider.CONTENT_URI_PUBLIC,
                            item.toContentValues());
                    }
                }
                ctx.sendBroadcast(new Intent(Constants.SERVICE_UPDATE_COMPLETED));
            }
        });
        ctx.sendBroadcast(new Intent(Constants.SERVICE_UPDATE_STARTED));
        fetchTaskPublic.execute(request);
    }

    private void fetchPrivate(final User authUser) {
        final Context ctx = this;
        String requestUrl = getString(R.string.url_private_aps);
        RestRequest request = new GetRestRequest(requestUrl, authUser.getEmail(), authUser.getPassword());
        RestTask<List<AccessPoint>> fetchTaskPrivate = new RestTask<List<AccessPoint>>(
            this, new AccessPointsFetchParser(false), new ResultListener<List<AccessPoint>>() {
            @Override
            public void onDownloadResult(List<AccessPoint> result) {
                if (result != null && !result.equals(Collections.emptyList())) {
                    for (AccessPoint item : result) {
                        if (item.getPrivacyType() == 1) {
                            getContentResolver().insert(
                                WifiContentProvider.CONTENT_URI_PRIVATE,
                                item.toContentValues());
                        } else {
                            item.setPublisherMail(authUser.getEmail());
                            getContentResolver().insert(
                                WifiContentProvider.CONTENT_URI_PUBLIC,
                                item.toContentValues());
                        }
                    }
                }
                ctx.sendBroadcast(new Intent(Constants.SERVICE_UPDATE_COMPLETED));
            }
        });
        ctx.sendBroadcast(new Intent(Constants.SERVICE_UPDATE_STARTED));
        fetchTaskPrivate.execute(request);
    }

    private void putAccessPoint(final User authUser, final UpdateManager.UpdateTask updateTask) {
        final AccessPoint ap = updateTask.accessPoint;
        final Context ctx = this;
        if(ap == null) {
            Log.e(TAG, "AccessPoint is NULL");
        }
        String requestUrl = getString(R.string.url_edit_ap) + ap.getId();
        HashMap<String, String> putData = new HashMap<String, String>();
        putData.put("name", ap.getName());
        putData.put("password", ap.getPassword());
        putData.put("securityType", ap.getSecurityType());
        putData.put("privacyType", String.valueOf(ap.getPrivacyType()));
        putData.put("lat", String.valueOf(ap.getLatitude()));
        putData.put("lon", String.valueOf(ap.getLongitude()));
        putData.put("lastAccessed", Utils.formatDate(ap.getLastAccessed()));

        RestRequest request = new PutRestRequest(requestUrl, putData, authUser.getEmail(), authUser.getPassword());
        RestTask<Integer> putTaskPrivate = new RestTask<>(this, new AccessPointPutParser(), new ResultListener<Integer>() {
            @Override
            public void onDownloadResult(Integer result) {
                if (result == Constants.SERVICE_RESULT_UNREACHABLE)
                    Log.e(TAG, "Couldn't communicate with server");
                if (result == Constants.SERVICE_RESULT_UNAUTHORIZED)
                    Log.e(TAG, "You are not an owner of this AP");

                // result == number of affected rows
                Intent intent = new Intent(Constants.SERVICE_UPDATE_COMPLETED);
                intent.putExtra(Constants.SERVICE_UPDATE_RESULT_STATUS, result);
                intent.putExtra(Constants.SERVICE_UPDATE_RESULT_TASK, updateTask);
                ctx.sendBroadcast(intent);
            }
        });
        ctx.sendBroadcast(new Intent(Constants.SERVICE_UPDATE_STARTED));
        putTaskPrivate.execute(request);
    }
}
