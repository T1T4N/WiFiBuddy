package com.titantech.wifibuddy.controllers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingDownPanelLayout;
import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.Utils;


public class EditActivity extends ActionBarActivity
    implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener,
    SlidingDownPanelLayout.PanelSlideListener {
    private AccessPoint mEditItem;
    private GoogleMap mGoogleMap;
    private Marker mItemMarker;

    private SlidingDownPanelLayout mSlidingDownPanelLayout;
    private EditText mFieldName, mFieldBssid, mFieldPassword;
    private Spinner mFieldPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setupActionBar();

        Intent intent = getIntent();
        try {
            mEditItem = intent.getParcelableExtra(Constants.EXTRA_ACTION_EDIT);
        } catch (Exception e) {
            Log.e("CURSOR_ERROR", "Error onCreate");
            e.printStackTrace();
            finish();
        }
        setupInterface();
    }

    private void setupInterface(){
        MapFragment mapFragment = (MapFragment) getFragmentManager()
            .findFragmentById(R.id.map_edit);
        mapFragment.getMapAsync(this);

        mSlidingDownPanelLayout = (SlidingDownPanelLayout) findViewById(R.id.slidingLayout);
        mSlidingDownPanelLayout.setPanelSlideListener(this);
        mSlidingDownPanelLayout.setParallaxDistance(100);

        mFieldName = (EditText)findViewById(R.id.edit_field_name);
        mFieldBssid = (EditText)findViewById(R.id.edit_field_bssid);
        mFieldPassword = (EditText)findViewById(R.id.edit_field_password);
        mFieldPrivacy = (Spinner)findViewById(R.id.edit_field_privacy);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.privacy_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFieldPrivacy.setAdapter(adapter);

        mFieldName.setText(mEditItem.getName());
        mFieldBssid.setText(mEditItem.getBssid());
        mFieldPassword.setText(mEditItem.getPassword());
        mFieldPrivacy.setSelection(mEditItem.getPrivacyType());
    }

    private void setupActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.ic_menu_close);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d("EDIT_HOME", "Home button pressed");
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        final LatLng itemLocation = new LatLng(mEditItem.getLatitude(), mEditItem.getLongitude());
        mItemMarker = googleMap.addMarker(new MarkerOptions()
                .position(itemLocation)
                .title(mEditItem.getName())
        );

        final Context ctx = this;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                float currentZoom = mGoogleMap.getCameraPosition().zoom;
                mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
                Projection projection = mGoogleMap.getProjection();
                Point screenCoordinates = projection.toScreenLocation(itemLocation);
                screenCoordinates.offset(0, Utils.dpToPx(ctx, 120));
                LatLng newPos = projection.fromScreenLocation(screenCoordinates);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itemLocation, currentZoom));

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 14.0f));

                mItemMarker.setDraggable(false);
                mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
            }
        }, 500);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelOpened(View panel) {
        CameraUpdate pos = CameraUpdateFactory.newCameraPosition(
            CameraPosition.fromLatLngZoom(mItemMarker.getPosition(), 16.0f)
        );
        mGoogleMap.animateCamera(pos);
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
        mItemMarker.setDraggable(true);

        //Bug fix:  MarkerDragListener must be set in order to receive the
        //          draggable markers new position
        mGoogleMap.setOnMarkerDragListener(this);
    }

    @Override
    public void onPanelClosed(View panel) {
        float currentZoom = mGoogleMap.getCameraPosition().zoom;
        mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
        Projection projection = mGoogleMap.getProjection();
        Point screenCoordinates = projection.toScreenLocation(mItemMarker.getPosition());
        screenCoordinates.offset(0, Utils.dpToPx(this, 120));
        LatLng newPos = projection.fromScreenLocation(screenCoordinates);
        mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(currentZoom));

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 14.0f));

        mItemMarker.setDraggable(false);
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }
}
