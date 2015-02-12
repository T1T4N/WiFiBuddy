package com.titantech.wifibuddy.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.models.AccessPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Robert on 12.02.2015.
 */
public class ChooseItemsAdapter extends BaseAdapter {
    private List<ScanResult> mItems;
    private Context mContext;
    private LayoutInflater mInflater;

    public ChooseItemsAdapter(Context context){
        mItems = new ArrayList<ScanResult>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.item_choose, null);

            viewHolder = new ViewHolder();
            viewHolder.apName = (TextView) convertView.findViewById(R.id.choose_list_item_title);
            viewHolder.apSecurity = (TextView) convertView.findViewById(R.id.choose_list_item_subtitle);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ScanResult sr = mItems.get(position);
        viewHolder.apName.setText(sr.SSID);
        viewHolder.apSecurity.setText(sr.capabilities);

        return convertView;
    }
    public void add(ScanResult item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void addAll(List<ScanResult> items) {
        mItems.addAll(items);
        //Collections.sort(mItems);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetInvalidated();
    }

    private static class ViewHolder {
        TextView apName;
        TextView apSecurity;
    }
}
