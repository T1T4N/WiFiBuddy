package com.titantech.wifibuddy.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.Utils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Robert on 24.01.2015.
 */
public class WifiContentProvider extends ContentProvider {
    private WifiDbOpenHelper mHelper;
    // used for the UriMacher
    private static final int PUBLIC_MULTIPLE = 10;
    private static final int PUBLIC_SINGLE = 20;

    private static final int PRIVATE_MULTIPLE = 30;
    private static final int PRIVATE_SINGLE = 40;


    private static final String AUTHORITY = "com.titantech.wifibuddy.contentprovider";

    private static final String BASE_PATH_PUBLIC = "public";
    public static final Uri CONTENT_URI_PUBLIC = Uri.parse("content://" + AUTHORITY
        + "/" + BASE_PATH_PUBLIC);

    private static final String BASE_PATH_PRIVATE = "private";
    public static final Uri CONTENT_URI_PRIVATE = Uri.parse("content://" + AUTHORITY
        + "/" + BASE_PATH_PRIVATE);

    private static final UriMatcher sURIMatcher = new UriMatcher(
        UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PUBLIC, PUBLIC_MULTIPLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PUBLIC + "/#", PUBLIC_SINGLE);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PRIVATE, PRIVATE_MULTIPLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PRIVATE + "/#", PRIVATE_SINGLE);
    }

    @Override
    public boolean onCreate() {
        mHelper = new WifiDbOpenHelper(getContext());
        return true; //Successful load
    }


    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case PUBLIC_MULTIPLE:
                return "vnd.android.cursor.dir/vnd.com.titantech.wifibuddy.db.public";
            case PUBLIC_SINGLE:
                return "vnd.android.cursor.item/vnd.com.titantech.wifibuddy.db.public";
            case PRIVATE_MULTIPLE:
                return "vnd.android.cursor.dir/vnd.com.titantech.wifibuddy.db.private";
            case PRIVATE_SINGLE:
                return "vnd.android.cursor.item/vnd.com.titantech.wifibuddy.db.private";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    private void checkColumns(String[] projection) {
        String[] available = WifiDbOpenHelper.PROJECTION_ALL_PUBLIC;
        if (projection != null) {

            HashSet<String> requestedColumns = new HashSet<String>(
                Arrays.asList(projection));

            HashSet<String> availableColumns = new HashSet<String>(
                Arrays.asList(available));

            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                    "Unknown columns in projection");
            }
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor = null;
        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case PUBLIC_MULTIPLE:
                queryBuilder.setTables(WifiDbOpenHelper.TABLE_PUBLIC);
                break;
            case PUBLIC_SINGLE:
                queryBuilder.setTables(WifiDbOpenHelper.TABLE_PUBLIC);
                // adding the ID to the original query
                queryBuilder.appendWhere(WifiDbOpenHelper.INTERNAL_ID + "="
                    + uri.getLastPathSegment());
                break;

            case PRIVATE_MULTIPLE:
                if (projection == null) {
                    projection = WifiDbOpenHelper.PROJECTION_ALL_PRIVATE;
                    SQLiteDatabase db = mHelper.getReadableDatabase();

                    try {

                        queryBuilder.setTables(WifiDbOpenHelper.TABLE_PRIVATE);
                        String query1 = queryBuilder.buildQuery(projection, selection, null, null, null, null);

                        //Cursor cursor1 = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);

                        queryBuilder.setTables(WifiDbOpenHelper.TABLE_PUBLIC);
                        String query2 = queryBuilder.buildQuery(projection, selection, null, null, null, null);
                        //Cursor cursor2 = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);

                        //setNotificationUri causing RuntimeError in privateItems onLongClick when using MergeCursor
                        //cursor1.setNotificationUri(getContext().getContentResolver(), uri);
                        //cursor2.setNotificationUri(getContext().getContentResolver(), uri);
                        //MergeCursor m = new MergeCursor(new Cursor[] {new CrossProcessCursorWrapper(cursor1), new CrossProcessCursorWrapper(cursor2)});

                        String unionQuery = queryBuilder.buildUnionQuery(new String[]{query1, query2}, sortOrder, null);
                        Cursor m = db.rawQuery(unionQuery, null);
                        cursor = m;
                    } catch (Exception ex){
                        Log.e("CURSOR_ERROR", "Error ContentProvider query private");
                        ex.printStackTrace();
                    }
                } else {
                    queryBuilder.setTables(WifiDbOpenHelper.TABLE_PRIVATE);
                }
                break;
            case PRIVATE_SINGLE:
                queryBuilder.setTables(WifiDbOpenHelper.TABLE_PRIVATE);
                queryBuilder.appendWhere(WifiDbOpenHelper.INTERNAL_ID + "="
                    + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        try {
            if (cursor == null) {
                SQLiteDatabase db = mHelper.getReadableDatabase();
                cursor = queryBuilder.query(db, projection, selection,
                    selectionArgs, null, null, sortOrder);
            }
            // make sure that potential listeners are getting notified
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception ex){
            Log.e("CURSOR_ERROR", "Error ContentProvider 2");
            ex.printStackTrace();
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        if (uriType != PUBLIC_MULTIPLE && uriType != PRIVATE_MULTIPLE) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case PUBLIC_MULTIPLE:
                // TODO: Change to CONFLICT_IGNORE when done testing
                id = db.insertWithOnConflict(WifiDbOpenHelper.TABLE_PUBLIC, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                if(values.getAsString(WifiDbOpenHelper.COLUMN_PUBLISHER).equals(Utils.getAuthenticatedUser().getUserId())){
                    getContext().getContentResolver().notifyChange(CONTENT_URI_PRIVATE, null);
                }
                return Uri.parse(BASE_PATH_PUBLIC + "/" + id);

            case PRIVATE_MULTIPLE:
                id = db.insertWithOnConflict(WifiDbOpenHelper.TABLE_PRIVATE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_PATH_PRIVATE + "/" + id);

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int rowsUpdated;
        String id;
        String where;
        switch (uriType) {
            case PUBLIC_MULTIPLE:
                rowsUpdated = db.update(WifiDbOpenHelper.TABLE_PUBLIC,
                    values, selection, selectionArgs);
                break;
            case PUBLIC_SINGLE:
                id = uri.getLastPathSegment();
                where = WifiDbOpenHelper.INTERNAL_ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowsUpdated = db.update(WifiDbOpenHelper.TABLE_PUBLIC,
                    values, where, null);
                break;
            case PRIVATE_MULTIPLE:
                rowsUpdated = db.update(WifiDbOpenHelper.TABLE_PRIVATE,
                    values, selection, selectionArgs);
                break;
            case PRIVATE_SINGLE:
                id = uri.getLastPathSegment();
                where = WifiDbOpenHelper.INTERNAL_ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rowsUpdated = db.update(WifiDbOpenHelper.TABLE_PRIVATE, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            if(uriType == PUBLIC_SINGLE){
                getContext().getContentResolver().notifyChange(CONTENT_URI_PRIVATE, null);
            }
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        int rowsDeleted;
        String id, where;
        switch (uriType) {
            case PUBLIC_MULTIPLE:
                rowsDeleted = db.delete(WifiDbOpenHelper.TABLE_PUBLIC, selection, selectionArgs);
                break;
            case PUBLIC_SINGLE:
                id = uri.getLastPathSegment();
                where = WifiDbOpenHelper.INTERNAL_ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " and " + selection;
                }
                rowsDeleted = db.delete(WifiDbOpenHelper.TABLE_PUBLIC, where, null);

                // Bug fix: notify PrivateItemsFragment of change
                uri = Uri.parse(CONTENT_URI_PRIVATE + "/" + id);
                break;
            case PRIVATE_MULTIPLE:
                rowsDeleted = db.delete(WifiDbOpenHelper.TABLE_PRIVATE, selection, selectionArgs);
                break;
            case PRIVATE_SINGLE:
                id = uri.getLastPathSegment();
                where = WifiDbOpenHelper.INTERNAL_ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " and " + selection;
                }
                rowsDeleted = db.delete(WifiDbOpenHelper.TABLE_PRIVATE, where, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            if(uriType == PUBLIC_SINGLE){
                getContext().getContentResolver().notifyChange(CONTENT_URI_PRIVATE, null);
            }
        }
        return rowsDeleted;
    }
}
