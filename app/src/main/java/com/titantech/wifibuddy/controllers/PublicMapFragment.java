package com.titantech.wifibuddy.controllers;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapFragment;

public class PublicMapFragment extends MapFragment {
    private static final String TAG = "PUBLIC_MAP_FRAGMENT";

    public PublicMapFragment() {
        super();
    }

    public static PublicMapFragment newInstance() {
        PublicMapFragment fragment = new PublicMapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);

        Fragment fragment = getParentFragment();
        if (fragment != null && fragment instanceof OnMapReadyListener) {
            ((OnMapReadyListener) fragment).onMapReady();
        }
        return v;
    }

    public static interface OnMapReadyListener {
        void onMapReady();
    }
}