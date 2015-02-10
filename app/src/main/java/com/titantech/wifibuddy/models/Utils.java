package com.titantech.wifibuddy.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Robert on 08.02.2015.
 */
public class Utils {
    private static User mAuthenticatedUser;

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static User getAuthenticatedUser() {
        return mAuthenticatedUser;
    }

    public static void setAuthenticatedUser(Context context, User newUser) {
        mAuthenticatedUser = newUser;
        SharedPreferences.Editor editor = context.getSharedPreferences(
            Constants.PREFS_NAME, Activity.MODE_PRIVATE).edit();
        editor.putString("id", newUser.getUserId());
        editor.putString("email", newUser.getEmail());
        editor.putString("password", newUser.getPassword());
        editor.apply();
    }

    public static void setAuthenticatedUser(
        Context context, String userId, String userEmail, String userPassword) {
        User newUser = new User(userId, userEmail, userPassword);
        setAuthenticatedUser(context, newUser);
    }

    public static int dpToPx(Context context, double dp){
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp*density + 0.5f);
    }
}
