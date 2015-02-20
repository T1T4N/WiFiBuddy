package com.titantech.wifibuddy.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.titantech.wifibuddy.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String STATE_SELECTED_TYPE = "selected_navigation_drawer_type";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String TAG = "NAVIGATION_DRAWER";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private static final int[] item_icons = {
        R.drawable.ic_account_box, R.drawable.ic_assignment, R.drawable.ic_track_changes, R.drawable.ic_explore,
        R.drawable.ic_settings, R.drawable.ic_info};

    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawerWhole;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mCurrentSelectedType = false;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("NAVIGATION_DRAWER", "onCreateView occurring");

        mDrawerWhole = (RelativeLayout) inflater.inflate(
            R.layout.fragment_navigation_drawer, container, false);

        mDrawerListView = (ListView) mDrawerWhole.findViewById(R.id.drawer_list);

        return mDrawerWhole;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION, 0);
            mCurrentSelectedType = savedInstanceState.getBoolean(STATE_SELECTED_TYPE, false);
            mFromSavedInstanceState = true;
        }
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);

        String[] itemTitles = getResources().getStringArray(R.array.drawer_labels);
        ArrayList<NavigationDrawerItem> drawerItems = new ArrayList<NavigationDrawerItem>();

        for (int i = 0; i < itemTitles.length; i++) {
            drawerItems.add(new NavigationDrawerItem(itemTitles[i], item_icons[i]));
        }

        NavigationDrawerAdapter drawerItemsAdapter = new NavigationDrawerAdapter(
            getActivity(),
            drawerItems,
            mCurrentSelectedPosition
        );
        mDrawerListView.setAdapter(drawerItemsAdapter);
        mDrawerListView.setOnItemClickListener(drawerItemsAdapter);
        mDrawerListView.setOnItemLongClickListener(drawerItemsAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition, mCurrentSelectedType);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
            getActivity(),                    /* host Activity */
            mDrawerLayout,                    /* DrawerLayout object */
            //R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
            R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
            R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void selectItem(int position, boolean longClick) {
        mCurrentSelectedPosition = position;
        mCurrentSelectedType = longClick;

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position, longClick);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putBoolean(STATE_SELECTED_TYPE, mCurrentSelectedType);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        /*
        if (item.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setTitle(R.string.app_name);
        } else {
            Log.e(TAG, "ActionBar is null");
        }
    }

    private ActionBar getActionBar() {
        Activity activity = getActivity();
        if(activity != null)
            return ((ActionBarActivity) activity).getSupportActionBar();
        return null;
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position, boolean longClick);
    }

    public class NavigationDrawerItem {
        public int icon;
        public String title;

        public NavigationDrawerItem() {
            super();
        }

        public NavigationDrawerItem(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }
    }

    public class NavigationDrawerAdapter extends BaseAdapter
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        private LayoutInflater mInflater;
        private List<NavigationDrawerItem> data;
        private int mResource;
        private int mSelectedPosition;

        public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> data, int initialSelectedPosition) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mResource = R.layout.item_navigation_drawer;
            this.data = data;
            this.mSelectedPosition = initialSelectedPosition;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            NavigationDrawerItemHolder holder = null;

            if (row == null) {
                row = mInflater.inflate(mResource, parent, false);

                holder = new NavigationDrawerItemHolder();
                holder.imgIcon = (ImageView) row.findViewById(R.id.drawer_item_image);
                holder.lTitle = (TextView) row.findViewById(R.id.drawer_item_title);

                row.setTag(holder);
            } else {
                holder = (NavigationDrawerItemHolder) row.getTag();
            }

            NavigationDrawerItem item = data.get(position);
            if (holder.lTitle.getText().length() <= 0) {
                holder.lTitle.setText(item.title);
            }
            if (holder.imgIcon.getDrawable() == null) {
                // No need to do this for a fixed-size menu with no recycling
                // Heavy operation, could cause OutOfMemory
                holder.imgIcon.setImageResource(item.icon);
            }
            if (position == mSelectedPosition) {
                holder.imgIcon.setColorFilter(getResources().getColor(R.color.colorPrimary));
                row.setBackgroundColor(getResources().getColor(R.color.very_light_grey));
            }
            else {
                holder.imgIcon.setColorFilter(null);
                row.setBackgroundResource(0);
            }
            return row;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectedPosition = position;
            mCurrentSelectedType = false;
            selectItem(position, false);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectedPosition = position;
            mCurrentSelectedType = true;
            selectItem(position, true);
            return true;
        }

        private class NavigationDrawerItemHolder {
            ImageView imgIcon;
            TextView lTitle;
        }
    }
}
