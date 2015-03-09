package com.titantech.wifibuddy.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.Fragment;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shamanland.fab.FloatingActionButton;
import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.adapters.PrivateItemsAdapter;
import com.titantech.wifibuddy.controllers.listeners.OnFragmentInteractionListener;
import com.titantech.wifibuddy.controllers.listeners.SectionChangedListener;
import com.titantech.wifibuddy.controllers.listeners.SwipeDismissListViewTouchListener;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.provider.WifiContentProvider;
import com.titantech.wifibuddy.service.IntentFactory;

import java.util.HashMap;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link com.titantech.wifibuddy.controllers.listeners.OnFragmentInteractionListener}
 * interface.
 */
public class PrivateItemsFragment extends Fragment
    implements View.OnClickListener, AdapterView.OnItemLongClickListener,
        WifiStateDialog.WifiStateDialogListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "PRIVATE_FRAGMENT";
    private SectionChangedListener mSectionChangedListener;
    private HashMap<Integer, String> mSectionTitles;
    private WifiManager mWifiManager;
    /**
     * The fragment's ListView/GridView.
     */
    private StickyListHeadersListView mListViewPrivate;
    private SwipeDismissListViewTouchListener mSwipeDismissListener;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private PrivateItemsAdapter mAdapterPrivate;

    public static PrivateItemsFragment newInstance(int position) {
        PrivateItemsFragment fragment = new PrivateItemsFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PrivateItemsFragment() {
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

    @SuppressLint("UseSparseArrays")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private, container, false);

        // Set the adapter
        mListViewPrivate = (StickyListHeadersListView) view.findViewById(R.id.list_private);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.private_fab);
        fab.setOnClickListener(this);

        mListViewPrivate.setEmptyView(view.findViewById(android.R.id.empty));
        mSectionTitles = new HashMap<Integer, String>();
        mSectionTitles.put(0, getString(R.string.header_public));
        mSectionTitles.put(1, getString(R.string.header_private));

        try {
            initLoader();
            mAdapterPrivate = new PrivateItemsAdapter(getActivity(), null, mSectionTitles);
            mListViewPrivate.setAdapter(mAdapterPrivate);

            final LoaderManager.LoaderCallbacks<Cursor> callbacks = this;
            mSwipeDismissListener = new SwipeDismissListViewTouchListener(
                    mListViewPrivate.getWrappedList(), new SwipeDismissListViewTouchListener.DismissCallbacks() {
                @Override
                public boolean canDismiss(int position) {
                    return true;
                }

                @Override
                public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                    for (int position : reverseSortedPositions) {
                        Log.d(TAG, "Remove " + String.valueOf(position));
                        mAdapterPrivate.remove(position);
                    }
                    //getLoaderManager().restartLoader(Constants.LOADER_PRIVATE_ID, null, callbacks);
                }
            }
            );
            mListViewPrivate.setOnTouchListener(mSwipeDismissListener);
            mListViewPrivate.setOnScrollListener(mSwipeDismissListener.makeScrollListener());

            //Intent intent = IntentFactory.getPrivateItems(getActivity());
            //getActivity().startService(intent);

            mListViewPrivate.setOnItemLongClickListener(this);
            mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        } catch (Exception ex) {
            Log.e(TAG, "CURSOR_ERROR: Error onCreateView");
            ex.printStackTrace();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SERVICE_UPDATE_COMPLETED);
        getActivity().registerReceiver(statusReceiver, filter);
        */
    }

    @Override
    public void onStop() {
        super.onStop();
        // getActivity().unregisterReceiver(statusReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListViewPrivate.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        try {
            String[] projection = null;
            return new CursorLoader(
                getActivity(),
                WifiContentProvider.CONTENT_URI_PRIVATE,
                projection,
                WifiDbOpenHelper.COLUMN_PUBLISHER + "=\"" +
                    Utils.getAuthenticatedUser().getUserId() + "\"",
                null,
                WifiDbOpenHelper.COLUMN_PRIVACY + " DESC, " +
                    WifiDbOpenHelper.COLUMN_NAME + " ASC"
            );
        } catch (Exception ex) {
            Log.e(TAG, "CURSOR_ERROR: Error onCreateLoader");
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            mAdapterPrivate.swapCursor(data);
        } catch (Exception ex) {
            Log.e(TAG, "CURSOR_ERROR: Error onLoadFinished");
            ex.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        try {
            mAdapterPrivate.swapCursor(null);
        } catch (Exception ex) {
            Log.e(TAG, "CURSOR_ERROR: Error onLoaderReset");
            ex.printStackTrace();
        }
    }

    private void initLoader() {
        try {
            getLoaderManager().initLoader(Constants.LOADER_PRIVATE_ID, null, this);
        } catch (Exception ex) {
            Log.e(TAG, "CURSOR_ERROR: Error initLoader");
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(Utils.checkWifiAndEnable(this, mWifiManager)){
            openChooseDialog();
        }
    }

    private void openChooseDialog() {
        FragmentManager fm = getFragmentManager();
        ChooseNetworkDialog chooseNetworkDialog = new ChooseNetworkDialog();
        chooseNetworkDialog.show(fm, "dialog_choose_network");
    }

    @Override
    public void onFinishWifiStateDialog() {
        mWifiManager.setWifiEnabled(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    openChooseDialog();
                } else {
                    Toast.makeText(getActivity(), "WiFi is being enabled", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "WiFi is enabled. You can now scan for networks", Toast.LENGTH_SHORT).show();
                        }
                    }, 1000);
                }
            }
        }, 1000);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = (Cursor) mAdapterPrivate.getItem(position);

        AccessPoint ap = new AccessPoint(
            c.getInt(c.getColumnIndex(WifiDbOpenHelper.INTERNAL_ID)),
            c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_ID)),
            c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER)),
            null,
            c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_BSSID)),
            c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)),
            c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PASSWORD)),
            c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_SECURITY)),
            c.getInt(c.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY)),
            c.getDouble(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LAT)),
            c.getDouble(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LON)),
            c.getString(c.getColumnIndex(WifiDbOpenHelper.COLUMN_LASTACCESS))
        );

        Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(Constants.EXTRA_ACTION, Constants.ACTION_EDIT);
        intent.putExtra(Constants.EXTRA_ACTION_EDIT, ap);
        startActivity(intent);
        return true;
    }
}
