package com.titantech.wifibuddy.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Robert on 08.02.2015.
 */
public class Utils {
    private static User mAuthenticatedUser;

    @SuppressWarnings("SimpleDateFormat")
    public static SimpleDateFormat datetime_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static Date long_ago;

    static {
        try {
            long_ago = datetime_format.parse("1999-01-01T01:01:01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
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
    public static int pxToDp(Context context, int px){
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (px/density);
    }
}
