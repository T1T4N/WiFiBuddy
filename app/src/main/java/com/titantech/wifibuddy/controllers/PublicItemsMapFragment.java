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
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.controllers.listeners.OnFragmentInteractionListener;
import com.titantech.wifibuddy.controllers.listeners.SectionChangedListener;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.provider.WifiContentProvider;
import com.titantech.wifibuddy.service.IntentFactory;

/**
 * Created by Robert on 25.01.2015.
 */
public class PublicItemsMapFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>, LocationListener {

    private OnFragmentInteractionListener mListener;
    private SectionChangedListener mSectionChangedListener;
    private MapView mMapView;
    private GoogleMap mMap;

    private LocationManager mLocationManager;

    // TODO: Rename and change types of parameters
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
            mListener = (OnFragmentInteractionListener) activity;
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

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_public_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.map_public);
        //mMapView.setVisibility(View.INVISIBLE);
        // TODO: very slow
        mMapView.onCreate(savedInstanceState);
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMapView = new MapView(getActivity());
                mMapView.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                ));
                view.addView(mMapView);
                mMapView.onCreate(savedInstanceState);
            }
        }, 300);
        */
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle args = null;
        getLoaderManager().initLoader(Constants.LOADER_PUBLIC_ID, args, this);
        Intent intent = IntentFactory.getPublicItems(getActivity());
        getActivity().startService(intent);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean netEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !netEnabled) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1000, this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // mMapView.setVisibility(View.VISIBLE);
                mMap = googleMap;
                googleMap.setMyLocationEnabled(true);

                while (data.moveToNext()) {
                    googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(
                                            data.getDouble(data.getColumnIndex(WifiDbOpenHelper.COLUMN_LAT)),
                                            data.getDouble(data.getColumnIndex(WifiDbOpenHelper.COLUMN_LON))
                                    ))
                                    .title(
                                            data.getString(data.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME))
                                    )
                                    .snippet(
                                            "Added by: " +
                                                    data.getString(data.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER_MAIL))
                                    )
                            //.icon(
                            //    BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_info_details)
                            //)
                    );
                    CameraUpdate cameraUpdate = null;
                    Location lastKnownLocationGPS = mLocationManager.getLastKnownLocation(mLocationManager.GPS_PROVIDER);
                    Location lastKnownLocationNetwork = mLocationManager.getLastKnownLocation(mLocationManager.NETWORK_PROVIDER);
                    Location lastKnownLocationPassive = mLocationManager.getLastKnownLocation(mLocationManager.PASSIVE_PROVIDER);

                    if (lastKnownLocationGPS != null) {
                        CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocationGPS.getLatitude(), lastKnownLocationGPS.getLongitude()), 14);
                    } else if (lastKnownLocationNetwork != null) {
                        CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocationNetwork.getLatitude(), lastKnownLocationNetwork.getLongitude()), 14);
                    } else if (lastKnownLocationPassive != null) {
                        CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocationPassive.getLatitude(), lastKnownLocationPassive.getLongitude()), 14);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) mMapView.onLowMemory();
    }
}
