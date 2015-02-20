package com.titantech.wifibuddy.models.tasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.network.ResultListener;
import com.titantech.wifibuddy.provider.WifiContentProvider;

/**
 * Created by Robert on 20.02.2015.
 */
public class EntriesCountFetchTask extends AsyncTask<Void, Void, Integer[]> {
    private Context mContext;
    private ResultListener<Integer[]> mListener;

    public EntriesCountFetchTask(Context context, ResultListener<Integer[]> listener) {
        super();
        this.mContext = context;
        this.mListener = listener;
    }


    @Override
    protected Integer[] doInBackground(Void... params) {
        Integer[] ret =  new Integer[2];

        String[] projection = new String[] {WifiDbOpenHelper.INTERNAL_ID};
        Cursor privateCursor = mContext.getContentResolver().query(
                WifiContentProvider.CONTENT_URI_PRIVATE,
                projection,
                null,
                null,
                null
        );
        if(privateCursor != null && !privateCursor.isAfterLast()){
            ret[0] = privateCursor.getCount();
        } else {
            ret[0] = 0;
        }

        Cursor publicCursor = mContext.getContentResolver().query(
                WifiContentProvider.CONTENT_URI_PUBLIC,
                projection,
                WifiDbOpenHelper.COLUMN_PUBLISHER + "=\"" + Utils.getAuthenticatedUser().getUserId() + "\"",
                null,
                null
        );
        if(publicCursor != null && !publicCursor.isAfterLast()){
            ret[1] = publicCursor.getCount();
        } else {
            ret[1] = 0;
        }

        if(privateCursor != null) privateCursor.close();
        if(publicCursor != null) publicCursor.close();
        return ret;
    }

    @Override
    protected void onPostExecute(Integer[] integers) {
        super.onPostExecute(integers);
        mListener.onResultReceived(integers);
    }
}
