package com.titantech.wifibuddy;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.titantech.wifibuddy.controllers.*;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.UpdateManager;
import com.titantech.wifibuddy.models.User;
import com.titantech.wifibuddy.models.Utils;


public class MainActivity extends ActionBarActivity
    implements NavigationDrawerFragment.NavigationDrawerCallbacks,
    OnFragmentInteractionListener, SectionChangedListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private TextView mNavigationEmail;
    private ImageView mNavigationAvatar;
    private Fragment mFragmentProfile, mFragmentPrivateItems,
        mFragmentScanItems, mFragmentPublicItems, mFragmentPublicItemsMap;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static String[] mSectionTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User usr = getCredentials();
        if (usr.equals(User.genericUnauthorized())) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            if (Utils.getAuthenticatedUser() == null)
                Utils.setAuthenticatedUser(this, usr);
            UpdateManager.setupInstance(getApplicationContext());

            setContentView(R.layout.activity_main);

            setupActionBar();
            setupNavigationDrawer();
        }

    }

    private void setupNavigationDrawer() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
            getFragmentManager().findFragmentById(R.id.navigation_drawer);

        if (mSectionTitles == null)
            mSectionTitles = getResources().getStringArray(R.array.toolbar_titles);

        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
            R.id.navigation_drawer,
            drawerLayout);

        mNavigationEmail = (TextView) findViewById(R.id.navigation_text_email);
        mNavigationEmail.setText(Utils.getAuthenticatedUser().getEmail());

        mNavigationAvatar = (ImageView) findViewById(R.id.navigation_image_avatar);
        mNavigationAvatar.setImageBitmap(createRoundImage(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture))
        );
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    private Bitmap createRoundImage(Bitmap loadedImage) {
        Bitmap circleBitmap = Bitmap.createBitmap(loadedImage.getWidth(), loadedImage.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader(loadedImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(loadedImage.getWidth() / 2, loadedImage.getHeight() / 2, loadedImage.getWidth() / 2, paint);

        return circleBitmap;
    }

    private User getCredentials() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        String userId = prefs.getString("id", null);
        if (userId == null) return User.genericUnauthorized();

        String email = prefs.getString("email", null);
        if (email == null) return User.genericUnauthorized();

        String password = prefs.getString("password", null);
        if (password == null) return User.genericUnauthorized();

        return new User(userId, email, password);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, boolean longClick) {
        // update the main content by replacing fragments

        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                if (mFragmentProfile == null) {
                    mFragmentProfile = ProfileFragment.newInstance(position);
                }
                fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, mFragmentProfile)
                    .commit();

                // startActivity(new Intent(this, LoginActivity.class));
                break;
            case 1:
                if (mFragmentPrivateItems == null) {
                    mFragmentPrivateItems = PrivateItemsFragment.newInstance(position);
                }
                fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, mFragmentPrivateItems)
                    .commit();
                break;
            case 2:
                if (mFragmentScanItems == null) {
                    mFragmentScanItems = ScanItemsFragment.newInstance(position);
                }
                fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, mFragmentScanItems)
                    .commit();
                break;
            case 3:
                if (!longClick) {
                    if (Utils.isInternetAvailable(this)) {
                        if (mFragmentPublicItemsMap == null) {
                            mFragmentPublicItemsMap = PublicItemsMapFragment.newInstance(position);
                        }
                        fragmentManager
                            .beginTransaction()
                            .replace(R.id.container, mFragmentPublicItemsMap)
                            .commit();
                    } else {
                        if (mFragmentPublicItems == null) {
                            mFragmentPublicItems = PublicItemsFragment.newInstance(position);
                        }
                        fragmentManager
                            .beginTransaction()
                            .replace(R.id.container, mFragmentPublicItems)
                            .commit();
                    }
                } else {
                    if (mFragmentPublicItems == null) {
                        mFragmentPublicItems = PublicItemsFragment.newInstance(position);
                    }
                    fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, mFragmentPublicItems)
                        .commit();
                }
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onSectionChanged(int number) {
        //TODO NullPointerException ?
        try {
            mTitle = mSectionTitles[number];
        } catch(Exception ex){
            Log.e("SECTION_CHANGED", mTitle.toString());
            Log.e("SECTION_CHANGED", String.valueOf(mSectionTitles == null));
            if(mSectionTitles != null){
                Log.e("SECTION_CHANGED", String.valueOf(mSectionTitles.length));
                Log.e("SECTION_CHANGED", String.valueOf(number));
            }
        }
    }
}
