package com.titantech.wifibuddy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.UpdateManager;
import com.titantech.wifibuddy.models.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Robert on 25.01.2015.
 */
public class PrivateItemsAdapter extends CursorAdapter {
    private static final String TAG = "PRIVATE_ADAPTER";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private LayoutInflater mInflater;
    private boolean[] mSectionFlags;
    private Map<Integer, String> mSectionNames;

    public PrivateItemsAdapter(Context context, Cursor c, Map<Integer, String> sectionNames) {
        super(context, c);
        this.mInflater = LayoutInflater.from(context);
        this.mSectionNames = sectionNames;
        buildSectionFlags(c);
    }

    public PrivateItemsAdapter(Context context, Cursor c, boolean autoRequery, Map<Integer, String> sectionNames) {
        super(context, c, autoRequery);
        this.mInflater = LayoutInflater.from(context);
        this.mSectionNames = sectionNames;
        buildSectionFlags(c);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.item_private, null);
            viewHolder = new ViewHolder();
            viewHolder.sectionHeader = (TextView) convertView.findViewById(R.id.private_section_header);
            viewHolder.apName = (TextView) convertView.findViewById(R.id.private_field_name);
            viewHolder.apPassword = (TextView) convertView.findViewById(R.id.private_field_password);
            viewHolder.apDate = (TextView) convertView.findViewById(R.id.private_field_updated);

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
            Log.d(TAG, "SECTION_ERROR: Array out of bounds " + position + ": " + mSectionFlags.length);
        }

        viewHolder.apName.setText(c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)));
        viewHolder.apPassword.setText(c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PASSWORD)));
        try {
            Date date = Utils.parseDate(c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LASTACCESS)));
            viewHolder.apDate.setText(format.format(date));
        } catch (Exception ex){
            ex.printStackTrace();
        }
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
        Log.d(TAG, "Swapping adapter cursor");
        buildSectionFlags(newCursor);
        return super.swapCursor(newCursor);
    }

    private void buildSectionFlags(Cursor newCursor) {
        if (newCursor != null) {
            mSectionFlags = new boolean[newCursor.getCount()];

            newCursor.moveToPosition(-1);
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
    }

    public void remove(int position){
        try {
            Cursor c = (Cursor) getItem(position);
            AccessPoint ap = new AccessPoint(
                c.getInt(c.getColumnIndex(WifiDbOpenHelper.INTERNAL_ID)),
                c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_ID)),
                c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER)),
                null,
                c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_BSSID)),
                c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)),
                c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PASSWORD)),
                c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_SECURITY)),
                c.getInt(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY)),
                c.getDouble(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LAT)),
                c.getDouble(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LON)),
                c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LASTACCESS))
            );

            UpdateManager.getInstance().queueDelete(ap);
            notifyDataSetChanged();
        } catch(Exception ex){
            Log.e(TAG, "Error on remove");
            ex.printStackTrace();
        }
    }
    private static class ViewHolder {
        TextView sectionHeader;
        TextView apName;
        TextView apPassword;
        TextView apDate;
    }
}
