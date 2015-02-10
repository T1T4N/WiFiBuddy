package com.titantech.wifibuddy.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.titantech.wifibuddy.MainActivity;
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
        extras.putInt("action", Constants.SERVICE_ACTION_PRIVATE);
        ret.putExtras(extras);
        return ret;
    }

    public static Intent getPublicItems(Context ctx) {
        Intent ret = new Intent(ctx, DataManagerService.class);
        Bundle extras = new Bundle();
        extras.putParcelable("auth_user", Utils.getAuthenticatedUser());
        extras.putInt("action", Constants.SERVICE_ACTION_PUBLIC);
        ret.putExtras(extras);
        return ret;
    }
}
