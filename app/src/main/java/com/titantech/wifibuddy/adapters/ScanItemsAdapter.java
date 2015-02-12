package com.titantech.wifibuddy.adapters;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.controllers.ScanItemsFragment.QueryResult;
import com.titantech.wifibuddy.models.AccessPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Robert on 08.02.2015.
 */
public class ScanItemsAdapter extends BaseAdapter
    implements AdapterView.OnItemClickListener {
    private List<QueryResult> mItems;
    private Context mContext;
    private LayoutInflater mInflater;
    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;

    public ScanItemsAdapter(Context context, WifiManager wifiManager) {
        mItems = new ArrayList<QueryResult>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mWifiManager = wifiManager;

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
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
            convertView = this.mInflater.inflate(R.layout.item_scan, null);

            viewHolder = new ViewHolder();
            viewHolder.apName = (TextView) convertView.findViewById(R.id.scan_field_name);
            viewHolder.apPassword = (TextView) convertView.findViewById(R.id.scan_field_password);
            viewHolder.apPublisher = (TextView) convertView.findViewById(R.id.scan_field_publisher);
            viewHolder.apInfo = (ImageView) convertView.findViewById(R.id.scan_icon_info);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final QueryResult item = mItems.get(position);
        AccessPoint ap = item.mAccessPoint;
        viewHolder.apName.setText(ap.getName());
        viewHolder.apPassword.setText(ap.getPassword());
        viewHolder.apPublisher.setText(ap.getPublisherMail());

        if(item.isBssidDifferent || item.isNameDifferent){
            viewHolder.apInfo.setVisibility(View.VISIBLE);
            viewHolder.apInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg = "";
                    if(item.isBssidDifferent){
                        msg = mContext.getString(R.string.scan_bssid_different);
                    } else if (item.isNameDifferent){
                        msg = mContext.getString(R.string.scan_name_different);
                    }
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            viewHolder.apInfo.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        QueryResult item = mItems.get(position);
        AccessPoint ap = item.mAccessPoint;
        Log.d("WIFI", "trying to connect");

        String bssid = ap.getBssid();
        String ssid = ap.getName();
        if(!item.isNameDifferent && item.isBssidDifferent){
            bssid = item.mScanBssid;
        }
        if(!item.isBssidDifferent && item.isNameDifferent){
            ssid = item.mScanName;
        }
        if(item.isBssidDifferent && item.isNameDifferent){
            Log.e("SCAN_ADAPTER", "ERROR: Item's BSSID and Name are BOTH different");
        }

        WifiConfiguration wc = new WifiConfiguration();
        wc.BSSID = bssid;
        wc.SSID = "\"" + ssid + "\"";
        // TODO: WEP Keys implementation ?
        wc.preSharedKey = "\"" + ap.getPassword() + "\"";

        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        int netId = mWifiManager.addNetwork(wc);

        if (!mWifiManager.disconnect()) {
            Log.d("WIFI", "Failed to disconnect");
        }
        if (!mWifiManager.enableNetwork(netId, true)) {
            Log.d("WIFI", "Failed to enable network");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mContext.registerReceiver(mStatusReceiver, mIntentFilter);
                if (!mWifiManager.reconnect()) {
                    Log.d("WIFI", "Failed to reconnect");
                }
            }
        }, 800);
    }

    private void onConnectionError() {
        mContext.unregisterReceiver(mStatusReceiver);
        String msg = mContext.getString(R.string.scan_connection_error);
        Log.i("ERROR_AUTHENTICATING", msg);
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private void onConnectionSuccess() {
        mContext.unregisterReceiver(mStatusReceiver);
        String msg = mContext.getString(R.string.scan_connection_success);
        Log.i("AUTHENTICATION_SUCCESS", msg);
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //context = MainActivity
            String action = intent.getAction();
            if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                Log.d("WIFI_STATUS_RECEIVER", "SUPPLICANT_STATE_CHANGED_ACTION");
                SupplicantState supl_state = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                Log.d("WIFI_STATE", supl_state.toString());
                int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                Log.d("WIFI_ERROR", String.valueOf(supl_error));

                if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
                    onConnectionError();
                } else if (supl_error == -1 && supl_state == SupplicantState.COMPLETED) {
                    onConnectionSuccess();
                }
            }
        }
    };

    public void add(QueryResult item) {
        mItems.add(item);
        Collections.sort(mItems);
        notifyDataSetChanged();
    }

    public void addAll(List<QueryResult> items) {
        mItems.addAll(items);
        Collections.sort(mItems);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetInvalidated();
    }

    private static class ViewHolder {
        TextView apName;
        TextView apPassword;
        TextView apPublisher;
        ImageView apInfo;
    }
}
