package com.titantech.wifibuddy.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;

/**
 * Created by Robert on 25.01.2015.
 */
public class PublicItemsAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    public PublicItemsAdapter(Context context, Cursor c) {
        super(context, c);
        this.mInflater = LayoutInflater.from(context);
    }

    public PublicItemsAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.item_public, null);
            viewHolder = new ViewHolder();
            viewHolder.apName = (TextView) convertView.findViewById(R.id.lTitle_public);
            viewHolder.apPassword = (TextView) convertView.findViewById(R.id.lSubtitle_public);
            viewHolder.apPublisher = (TextView) convertView.findViewById(R.id.lSubtitle2_public);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Cursor c = getCursor();
        c.moveToPosition(position);

        viewHolder.apName.setText(c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)));
        viewHolder.apPassword.setText(
            "Password: " + c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PASSWORD)) +
                "  Security: " + c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_SECURITY))

        );
        viewHolder.apPublisher.setText(
                "Publisher: " + String.valueOf(c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER_MAIL)))
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

    private static class ViewHolder {
        TextView apName;
        TextView apPassword;
        TextView apPublisher;
    }
}
