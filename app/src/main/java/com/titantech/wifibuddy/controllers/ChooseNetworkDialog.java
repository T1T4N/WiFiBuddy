package com.titantech.wifibuddy.controllers;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.adapters.ChooseItemsAdapter;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.provider.WifiContentProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Robert on 12.02.2015.
 */
public class ChooseNetworkDialog  extends DialogFragment
        implements AdapterView.OnItemClickListener, LocationListener {
    private ChooseItemsAdapter mAdapter;
    private WifiManager mWifiManager;
    private ListView mListNetworks;
    private double mLatitude, mLongitude;
    public ChooseNetworkDialog () {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_choose_network, container);
        mListNetworks = (ListView) view.findViewById(R.id.dialog_choose_list);
        mListNetworks.setEmptyView(view.findViewById(android.R.id.empty));
        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        mAdapter = new ChooseItemsAdapter(getActivity());
        fillList();
        mListNetworks.setAdapter(mAdapter);
        setupLocation();
        return view;
    }

    private void fillList() {
        List<ScanResult> results = mWifiManager.getScanResults();
        results = mWifiManager.getScanResults();
        ArrayList<ScanResult> items = new ArrayList<ScanResult>();

        String message = null;
        if (results != null) {
            if (results.size() == 0) {
                message = getString(R.string.scan_none_in_range);
            } else {
                for(ScanResult res : results){
                    if(!isNetworkOwned(res)){
                        items.add(res);
                    }
                }
                if (items.size() == 0) {
                    message = getString(R.string.scan_not_found_range);
                }
            }
        } else {
            message = getString(R.string.scan_no_results);
        }
        if (message != null) {
            setEmptyText(message);
        } else {
            mAdapter.addAll(items);
        }
    }
    private void setupLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(gpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600, 1000, this);
                Toast.makeText(getActivity(), "Getting current location", Toast.LENGTH_SHORT).show();
            }
            Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastKnownLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location lastKnownLocationPassive =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (lastKnownLocationGPS != null) {
                mLatitude = lastKnownLocationGPS.getLatitude();
                mLongitude = lastKnownLocationGPS.getLongitude();
            } else if (lastKnownLocationNetwork != null){
                mLatitude = lastKnownLocationNetwork.getLatitude();
                mLongitude = lastKnownLocationNetwork.getLongitude();
            } else if (lastKnownLocationPassive != null){
                mLatitude = lastKnownLocationPassive.getLatitude();
                mLongitude = lastKnownLocationPassive.getLongitude();
            } else {
                mLatitude = mLongitude = 0.0;
            }
        } else {
            mLatitude = mLongitude = 0.0;
        }
        mListNetworks.setOnItemClickListener(this);
    }
    private boolean isNetworkOwned(ScanResult sr){
        Cursor resPrivate = getActivity().getContentResolver().query(
                WifiContentProvider.CONTENT_URI_PRIVATE,
                null,
                WifiDbOpenHelper.COLUMN_PUBLISHER + "=\"" +
                        Utils.getAuthenticatedUser().getUserId() + "\" AND " +
                        WifiDbOpenHelper.COLUMN_BSSID + "=\"" + sr.BSSID + "\"",
                null,
                null
        );
        return !resPrivate.isAfterLast();
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult sr = (ScanResult)mAdapter.getItem(position);
        AccessPoint ap = new AccessPoint(
                "<unspecified>",
                Utils.getAuthenticatedUser().getUserId(),
                Utils.getAuthenticatedUser().getEmail(),
                sr.BSSID,
                sr.SSID,
                "",
                sr.capabilities,
                0,
                mLatitude,
                mLongitude,
                Utils.formatDate()
        );

        Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(Constants.EXTRA_ACTION, Constants.ACTION_ADD);
        intent.putExtra(Constants.EXTRA_ACTION_EDIT, ap);
        startActivity(intent);
        dismiss();
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListNetworks.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        Toast.makeText(getActivity(), "Location updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}