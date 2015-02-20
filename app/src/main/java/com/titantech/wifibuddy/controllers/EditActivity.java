package com.titantech.wifibuddy.controllers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.titantech.wifibuddy.models.ui.SlidingDownPanelLayout;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.UpdateManager;
import com.titantech.wifibuddy.models.Utils;

import java.util.Date;


public class EditActivity extends ActionBarActivity
    implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener,
    SlidingDownPanelLayout.PanelSlideListener {

    private static final String TAG = "EDIT_ACTIVITY";
    private AccessPoint mEditItem;
    private GoogleMap mGoogleMap;
    private Marker mItemMarker;
    private InputMethodManager mInputMethodManager;

    private SlidingDownPanelLayout mSlidingDownPanelLayout;
    private EditText mFieldName, mFieldBssid, mFieldPassword, mFieldSecurity;
    private Spinner mFieldPrivacy;
    private TextView mFieldLat, mFieldLon;
    private boolean mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setupActionBar();

        Intent intent = getIntent();
        try {
            mEditItem = intent.getParcelableExtra(Constants.EXTRA_ACTION_EDIT);
            mEdit = intent.getIntExtra(Constants.EXTRA_ACTION, Constants.ACTION_EDIT) == Constants.ACTION_EDIT;
        } catch (Exception e) {
            Log.e(TAG, "Error onCreate");
            e.printStackTrace();
            finish();
        }
        setupInterface();
    }

    private void setupInterface() {
        if(mEdit) {
            setTitle(getString(R.string.title_activity_edit));
        } else {
            setTitle(getString(R.string.title_activity_add));
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager()
            .findFragmentById(R.id.map_edit);
        mapFragment.getMapAsync(this);

        mSlidingDownPanelLayout = (SlidingDownPanelLayout) findViewById(R.id.slidingLayout);
        mSlidingDownPanelLayout.setPanelSlideListener(this);
        mSlidingDownPanelLayout.setSliderFadeColor(Color.argb(128, 0, 0, 0));
        mSlidingDownPanelLayout.setParallaxDistance(100);

        mFieldName = (EditText) findViewById(R.id.edit_field_name);
        mFieldBssid = (EditText) findViewById(R.id.edit_field_bssid);
        mFieldPassword = (EditText) findViewById(R.id.edit_field_password);
        mFieldSecurity = (EditText) findViewById(R.id.edit_field_security);
        mFieldPrivacy = (Spinner) findViewById(R.id.edit_field_privacy);
        mFieldLat = (TextView) findViewById(R.id.edit_field_latitude);
        mFieldLon = (TextView) findViewById(R.id.edit_field_longitude);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.privacy_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFieldPrivacy.setAdapter(adapter);

        mFieldName.setText(mEditItem.getName());
        mFieldBssid.setText(mEditItem.getBssid());
        mFieldPassword.setText(mEditItem.getPassword());
        mFieldSecurity.setText(mEditItem.getSecurityType());
        mFieldPrivacy.setSelection(mEditItem.getPrivacyType());
        mFieldLat.setText(String.format("%.5f", mEditItem.getLatitude()));
        mFieldLon.setText(String.format("%.5f", mEditItem.getLongitude()));

        if(mEdit){
            mFieldName.setEnabled(true);
        } else {
            mFieldName.setEnabled(false);
        }
    }

    private void setupActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.ic_menu_close);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private void updateAccessPoint() {
        if(validateFields()) {
            AccessPoint oldAp = new AccessPoint(mEditItem);
            mEditItem.setName(mFieldName.getText().toString());
            mEditItem.setPassword(mFieldPassword.getText().toString());
            mEditItem.setPrivacyType(mFieldPrivacy.getSelectedItemPosition());
            mEditItem.setLatitude(mItemMarker.getPosition().latitude);
            mEditItem.setLongitude(mItemMarker.getPosition().longitude);
            mEditItem.setLastAccessed(new Date());

            if (mEdit) {
                if (!oldAp.equals(mEditItem)) {  // If change occurred
                    if (oldAp.getPrivacyType() == mEditItem.getPrivacyType()) {
                        UpdateManager.getInstance().queueUpdate(mEditItem);
                    } else {    // Need to remove the entry from the previous table and insert it in the new
                        UpdateManager.getInstance().queueDelete(oldAp);
                        UpdateManager.getInstance().queueInsert(mEditItem);
                    }
                }
            } else {
                if (mEditItem.getPrivacyType() == 1) {   // Private items have no publisher mail in the db
                    mEditItem.setPublisherMail(null);
                }
                UpdateManager.getInstance().queueInsert(mEditItem);
            }
            finish();
        }
    }

    public boolean validateFields() {
        mFieldName.setError(null);
        mFieldPassword.setError(null);

        // Store values at the time of the save attempt.
        String name = mFieldName.getText().toString();
        String password = mFieldPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || password.length() < 5) {
            mFieldPassword.setError(getString(R.string.error_invalid_password));
            focusView = mFieldPassword;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(name)) {
            mFieldName.setError(getString(R.string.error_field_required));
            focusView = mFieldName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }
        return !cancel;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_save:
                updateAccessPoint();
                return true;
            /*
            case R.id.action_settings:
                return true;
            */
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
        mGoogleMap.setMyLocationEnabled(true);

        final LatLng itemLocation = new LatLng(mEditItem.getLatitude(), mEditItem.getLongitude());
        mItemMarker = googleMap.addMarker(new MarkerOptions()
                .position(itemLocation)
                .title(mEditItem.getName())
        );

        final Context ctx = this;
        // Bug fix: Update camera after 500ms so the map tiles can initially load
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

                mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(mEdit) {
                    mInputMethodManager.toggleSoftInputFromWindow(mFieldName.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                    mFieldName.requestFocus();
                } else {
                    mInputMethodManager.toggleSoftInputFromWindow(mFieldPassword.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                    mFieldPassword.requestFocus();
                }
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

        //Bug fix:  MarkerDragListener must be set in order to receive the draggable marker's new position
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
    protected void onPause() {
        super.onPause();
        try {
            mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onPause Keyboard hide error");
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mFieldLat.setText(String.format("%.5f", marker.getPosition().latitude));
        mFieldLon.setText(String.format("%.5f", marker.getPosition().longitude));
    }
}
