package com.titantech.wifibuddy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;

import java.util.Map;

/**
 * Created by Robert on 25.01.2015.
 */
public class PrivateItemsAdapter extends CursorAdapter {
    private LayoutInflater mInflater;
    private boolean[] mSectionFlags;
    private Map<Integer, String> mSectionNames;

    public PrivateItemsAdapter(Context context, Cursor c, Map<Integer, String> sectionNames) {
        super(context, c);
        this.mInflater = LayoutInflater.from(context);
        this.mSectionNames = sectionNames;

        if (c != null) {
            mSectionFlags = new boolean[c.getCount()];

            int prevType = -1;
            while (c.moveToNext()) {
                int cType = c.getInt(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY));
                if (cType != prevType) {
                    mSectionFlags[c.getPosition()] = true;
                }
                prevType = cType;
            }
            c.moveToFirst();
        }
    }

    public PrivateItemsAdapter(Context context, Cursor c, boolean autoRequery, Map<Integer, String> sectionNames) {
        super(context, c, autoRequery);
        this.mInflater = LayoutInflater.from(context);
        this.mSectionNames = sectionNames;

        if (c != null) {
            mSectionFlags = new boolean[c.getCount()];

            int prevType = -1;
            while (c.moveToNext()) {
                int cType = c.getInt(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY));
                if (cType != prevType) {
                    mSectionFlags[c.getPosition()] = true;
                }
                prevType = cType;
            }
            c.moveToFirst();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.item_private, null);
            viewHolder = new ViewHolder();
            viewHolder.sectionHeader = (TextView) convertView.findViewById(R.id.section_header_private);
            viewHolder.apName = (TextView) convertView.findViewById(R.id.lTitle_private);
            viewHolder.apPassword = (TextView) convertView.findViewById(R.id.lSubtitle_private);
            viewHolder.apPrivacy = (TextView) convertView.findViewById(R.id.lSubtitle2_private);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Cursor c = getCursor();
        c.moveToPosition(position);

        int itemPrivacy = c.getInt(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY));

        try {
            if (mSectionFlags[position]) {
                viewHolder.sectionHeader.setVisibility(View.VISIBLE);
                viewHolder.sectionHeader.setText(mSectionNames.get(itemPrivacy));
            } else {
                viewHolder.sectionHeader.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Log.d("SECTION_ERROR", "Array out of bounds " + position + ": " + mSectionFlags.length);
        }

        viewHolder.apName.setText(c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)));
        viewHolder.apPassword.setText(
            "Password: " + c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PASSWORD)) +
                "    lat: " + c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LAT)) +
                " lon: " + c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LON))
        );
        viewHolder.apPrivacy.setText(
            "Internal id: " + String.valueOf(c.getInt(c.getColumnIndex(WifiDbOpenHelper.INTERNAL_ID))) +
                "   Privacy type: " + String.valueOf(itemPrivacy)
        );
        return convertView;
    }

    /* (non-Javadoc)
     * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Dont need to do anything here

    }

    /* (non-Javadoc)
     * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Dont need to do anything here either
        return null;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor != null) {
            mSectionFlags = new boolean[newCursor.getCount()];

            int prevType = -1;
            while (newCursor.moveToNext()) {
                int cType = newCursor.getInt(newCursor.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY));
                if (cType != prevType) {
                    mSectionFlags[newCursor.getPosition()] = true;
                }
                prevType = cType;
            }
            newCursor.moveToFirst();
        }
        return super.swapCursor(newCursor);
    }

    private static class ViewHolder {
        TextView sectionHeader;
        TextView apName;
        TextView apPassword;
        TextView apPrivacy;
    }
}
