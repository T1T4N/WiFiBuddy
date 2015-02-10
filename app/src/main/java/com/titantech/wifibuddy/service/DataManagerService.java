package com.titantech.wifibuddy.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.User;
import com.titantech.wifibuddy.network.RestTask;
import com.titantech.wifibuddy.network.ResultListener;
import com.titantech.wifibuddy.network.requests.GetRestRequest;
import com.titantech.wifibuddy.network.requests.RestRequest;
import com.titantech.wifibuddy.parsers.AccessPointsFetchParser;
import com.titantech.wifibuddy.provider.WifiContentProvider;

import java.util.Collections;
import java.util.List;

/**
 * Created by Robert on 24.01.2015.
 */
public class DataManagerService extends IntentService {

    public DataManagerService() {
        super("datamanager-service");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle data = intent.getExtras();
        if (data != null) {
            final User authUser = data.getParcelable("auth_user");
            int action = data.getInt("action");
            String requestUrl;
            RestRequest request;
            final Context ctx = this;
            switch (action) {
                case Constants.SERVICE_ACTION_PRIVATE:
                    requestUrl = getString(R.string.url_private_aps);
                    request = new GetRestRequest(requestUrl, authUser.getEmail(), authUser.getPassword());
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
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fetchTaskPrivate.execute(request);
                    break;
                case Constants.SERVICE_ACTION_PUBLIC:
                    requestUrl = getString(R.string.url_public_aps);
                    request = new GetRestRequest(requestUrl, authUser.getEmail(), authUser.getPassword());
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
                    break;
            }
        }
    }
}
