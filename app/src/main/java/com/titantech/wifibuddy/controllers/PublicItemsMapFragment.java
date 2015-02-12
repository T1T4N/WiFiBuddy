package com.titantech.wifibuddy.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.controllers.listeners.SectionChangedListener;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.provider.WifiContentProvider;
import com.titantech.wifibuddy.service.IntentFactory;

public class PublicItemsMapFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>, LocationListener, PublicMapFragment.OnMapReadyListener {

    private static final String TAG = "PUBLIC_MAP";
    private LinearLayout mRootView;
    private SectionChangedListener mSectionChangedListener;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    public static PublicItemsMapFragment newInstance(int position) {
        PublicItemsMapFragment fragment = new PublicItemsMapFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    public PublicItemsMapFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSectionChangedListener = (SectionChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        mSectionChangedListener.onSectionChanged(getArguments().getInt(Constants.ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // mParam1 = getArguments().getString(ARG_PARAM1);
            // mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        mRootView = (LinearLayout)inflater.inflate(R.layout.fragment_public_map, container, false);
        setupMapFragment();
        return mRootView;
    }

    private void setupMapFragment() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMapFragment = PublicMapFragment.newInstance();
                getChildFragmentManager().beginTransaction().replace(R.id.map_public, mMapFragment).commit();
            }
        }, 150);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean netEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !netEnabled) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1000, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = WifiDbOpenHelper.PROJECTION_ALL_PUBLIC;
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                WifiContentProvider.CONTENT_URI_PUBLIC, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                googleMap.setMyLocationEnabled(true);

                while (data.moveToNext()) {
                    googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(
                                            data.getDouble(data.getColumnIndex(WifiDbOpenHelper.COLUMN_LAT)),
                                            data.getDouble(data.getColumnIndex(WifiDbOpenHelper.COLUMN_LON))
                                    ))
                                    .title(data.getString(data.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)))
                                    .snippet(
                                            "Added by: " + data.getString(data.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER_MAIL))
                                    )
                            //.icon(
                            //    BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_info_details)
                            //)
                    );
                    CameraUpdate cameraUpdate = null;
                    Location lastKnownLocationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location lastKnownLocationNetwork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Location lastKnownLocationPassive = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                    if (lastKnownLocationGPS != null) {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocationGPS.getLatitude(), lastKnownLocationGPS.getLongitude()), 14);
                    } else if (lastKnownLocationNetwork != null) {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocationNetwork.getLatitude(), lastKnownLocationNetwork.getLongitude()), 14);
                    } else if (lastKnownLocationPassive != null) {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocationPassive.getLatitude(), lastKnownLocationPassive.getLongitude()), 14);
                    } else {
                        cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(41.6137143, 21.743258), 8);
                    }
                    googleMap.animateCamera(cameraUpdate);
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
            mMap.animateCamera(cameraUpdate);
        }
        mLocationManager.removeUpdates(this);
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mAdapter.swapCursor(null);
    }

    @Override
    public void onMapReady() {
        getLoaderManager().initLoader(Constants.LOADER_PUBLIC_ID, null, this);
        // TODO: Remove this code, will be handled by UpdateManager
        Intent intent = IntentFactory.getPublicItems(getActivity());
        getActivity().startService(intent);
    }
}
