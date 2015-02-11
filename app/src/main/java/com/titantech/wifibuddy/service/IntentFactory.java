package com.titantech.wifibuddy.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.Utils;

/**
 * Created by Robert on 24.01.2015.
 */
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

    public static Intent putItem(Context ctx, AccessPoint item) {
        Intent ret = new Intent(ctx, DataManagerService.class);
        Bundle extras = new Bundle();
        extras.putParcelable("auth_user", Utils.getAuthenticatedUser());
        extras.putInt("action", Constants.SERVICE_ACTION_PUT);
        extras.putParcelable("update_item", item);
        ret.putExtras(extras);
        return ret;
    }
}
