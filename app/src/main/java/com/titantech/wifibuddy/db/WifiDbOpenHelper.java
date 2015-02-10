package com.titantech.wifibuddy.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

/**
 * Created by Robert on 23.01.2015.
 */
public class WifiDbOpenHelper extends SQLiteOpenHelper {
    public static final String TABLE_PUBLIC = "PublicAP";
    public static final String TABLE_PRIVATE = "UserAP";

    public static final String INTERNAL_ID = "_id";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PUBLISHER = "publisherId";
    public static final String COLUMN_PUBLISHER_MAIL = "publisherMail";
    public static final String COLUMN_BSSID = "bssid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_SECURITY = "securityType";
    public static final String COLUMN_PRIVACY = "privacyType";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_LASTACCESS = "lastAccessed";

    public static final String[] PROJECTION_ALL_PRIVATE = {
        INTERNAL_ID,
        COLUMN_ID,
        COLUMN_PUBLISHER,
        COLUMN_BSSID,
        COLUMN_NAME,
        COLUMN_PASSWORD,
        COLUMN_SECURITY,
        COLUMN_PRIVACY,
        COLUMN_LAT,
        COLUMN_LON,
        COLUMN_LASTACCESS
    };
    public static final String[] PROJECTION_ALL_PUBLIC = {
        INTERNAL_ID,
        COLUMN_ID,
        COLUMN_PUBLISHER,
        COLUMN_PUBLISHER_MAIL,
        COLUMN_BSSID,
        COLUMN_NAME,
        COLUMN_PASSWORD,
        COLUMN_SECURITY,
        COLUMN_PRIVACY,
        COLUMN_LAT,
        COLUMN_LON,
        COLUMN_LASTACCESS
    };

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME_EXPRESSION = "WiFiDatabase.db";

    private static final String DATABASE_CREATE_PUBLIC = String.format(
        "create table %s (" +
            "_id integer primary key autoincrement, " +
            "%s text not null unique, " +
            "%s text not null, " +
            "%s text, " +
            "%s text not null unique, " +
            "%s text not null, " +
            "%s text not null, " +
            "%s text not null, " +
            "%s integer not null, " +
            "%s real not null, " +
            "%s real not null, " +
            "%s datetime not null" +
            ");",
        TABLE_PUBLIC,
        COLUMN_ID, COLUMN_PUBLISHER, COLUMN_PUBLISHER_MAIL, COLUMN_BSSID, COLUMN_NAME,
        COLUMN_PASSWORD, COLUMN_SECURITY, COLUMN_PRIVACY, COLUMN_LAT, COLUMN_LON, COLUMN_LASTACCESS
    );
    private static final String DATABASE_CREATE_PRIVATE = String.format(
        "create table %s (" +
            "_id integer primary key autoincrement, " +
            "%s text not null unique, " +
            "%s text not null, " +
            "%s text not null unique, " +
            "%s text not null, " +
            "%s text not null, " +
            "%s text not null, " +
            "%s integer not null, " +
            "%s real not null, " +
            "%s real not null, " +
            "%s datetime not null" +
            ");",
        TABLE_PRIVATE, COLUMN_ID, COLUMN_PUBLISHER, COLUMN_BSSID, COLUMN_NAME,
        COLUMN_PASSWORD, COLUMN_SECURITY, COLUMN_PRIVACY, COLUMN_LAT, COLUMN_LON, COLUMN_LASTACCESS
    );

    public WifiDbOpenHelper(Context context) {
        super(context, String.format(DATABASE_NAME_EXPRESSION), null,
            DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_PUBLIC);
        database.execSQL(DATABASE_CREATE_PRIVATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_PUBLIC));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_PRIVATE));
        onCreate(db);
    }
}
