package com.titantech.wifibuddy.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.UpdateManager;
import com.titantech.wifibuddy.models.Utils;

import java.util.ArrayList;
import java.util.List;

public class IntentFactory {
    public static Intent getPrivateItems(Context ctx) {
        Intent ret = new Intent(ctx, DataManagerService.class);
        Bundle extras = new Bundle();
        extras.putParcelable("auth_user", Utils.getAuthenticatedUser());
        extras.putInt("action", Constants.SERVICE_ACTION_GET_PRIVATE);
        ret.putExtras(extras);
        return ret;
    }

    public static Intent getPublicItems(Context ctx) {
        Intent ret = new Intent(ctx, DataManagerService.class);
        Bundle extras = new Bundle();
        extras.putParcelable("auth_user", Utils.getAuthenticatedUser());
        extras.putInt("action", Constants.SERVICE_ACTION_GET_PUBLIC);
        ret.putExtras(extras);
        return ret;
    }

    public static Intent batchActions(Context ctx, ArrayList<UpdateManager.UpdateTask> tasks) {
        Intent ret = new Intent(ctx, DataManagerService.class);
        Bundle extras = new Bundle();
        extras.putParcelable("auth_user", Utils.getAuthenticatedUser());
        extras.putInt("action", Constants.SERVICE_ACTION_BATCH_TASKS);
        extras.putParcelableArrayList("update_item", tasks);
        ret.putExtras(extras);
        return ret;
    }
}
