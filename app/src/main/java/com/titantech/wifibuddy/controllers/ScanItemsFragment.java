package com.titantech.wifibuddy.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.adapters.ScanItemsAdapter;
import com.titantech.wifibuddy.controllers.listeners.SectionChangedListener;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.provider.WifiContentProvider;
import java.util.ArrayList;
import java.util.List;

public class ScanItemsFragment extends Fragment
    implements View.OnClickListener, WifiStateDialog.WifiStateDialogListener {

    private SectionChangedListener mSectionChangedListener;
    private WifiManager mWifiManager;
    private ContentResolver mContentResolver;

    private AbsListView mListView;
    private Button mScanButton;

    private ScanItemsAdapter mAdapter;

    public static ScanItemsFragment newInstance(int position) {
        ScanItemsFragment fragment = new ScanItemsFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScanItemsFragment() {
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
        /*
        if (getArguments() != null) {
            // mParam1 = getArguments().getString(ARG_PARAM1);
            // mParam2 = getArguments().getString(ARG_PARAM2);
        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_scan, container, false);

        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        mListView = (AbsListView) view.findViewById(R.id.list_scan);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));
        mAdapter = new ScanItemsAdapter(getActivity(), mWifiManager);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);

        mScanButton = (Button) view.findViewById(R.id.button_scan);
        mScanButton.setOnClickListener(this);

        mContentResolver = getActivity().getContentResolver();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private QueryResult queryNetwork(String bssid, String name, int level) {
        String[] selection = new String[] {bssid, name};
        Cursor resPrivate = mContentResolver.query(
            WifiContentProvider.CONTENT_URI_PRIVATE,
            WifiDbOpenHelper.PROJECTION_ALL_PRIVATE,
            WifiDbOpenHelper.COLUMN_BSSID + "=? OR " +
                WifiDbOpenHelper.COLUMN_NAME + "=?",
            selection,
            null
        );
        if (!resPrivate.isAfterLast()) { //found
            resPrivate.moveToNext();
            AccessPoint ap = new AccessPoint(
                resPrivate.getInt(resPrivate.getColumnIndex(WifiDbOpenHelper.INTERNAL_ID)),
                resPrivate.getString(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_ID)),
                resPrivate.getString(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER)),
                "You",
                resPrivate.getString(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_BSSID)),
                resPrivate.getString(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)),
                resPrivate.getString(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_PASSWORD)),
                resPrivate.getString(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_SECURITY)),
                resPrivate.getInt(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY)),
                resPrivate.getDouble(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_LAT)),
                resPrivate.getDouble(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_LON)),
                resPrivate.getString(resPrivate.getColumnIndex(WifiDbOpenHelper.COLUMN_LASTACCESS))
            );
            // resPrivate.close();
            return new QueryResult(bssid, name, level, ap);
        } else {
            // resPrivate.close();
            Cursor resPublic = mContentResolver.query(
                WifiContentProvider.CONTENT_URI_PUBLIC,
                WifiDbOpenHelper.PROJECTION_ALL_PUBLIC,
                WifiDbOpenHelper.COLUMN_BSSID + "=? OR " +
                    WifiDbOpenHelper.COLUMN_NAME + "=?",
                selection,
                null
            );
            if (!resPublic.isAfterLast()) { //found
                resPublic.moveToNext();
                AccessPoint ap = new AccessPoint(
                    resPublic.getInt(resPublic.getColumnIndex(WifiDbOpenHelper.INTERNAL_ID)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_ID)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_PUBLISHER_MAIL)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_BSSID)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_NAME)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_PASSWORD)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_SECURITY)),
                    resPublic.getInt(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_PRIVACY)),
                    resPublic.getDouble(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_LAT)),
                    resPublic.getDouble(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_LON)),
                    resPublic.getString(resPublic.getColumnIndex(WifiDbOpenHelper.COLUMN_LASTACCESS))
                );
                // resPublic.close();
                return new QueryResult(bssid, name, level, ap);
            } else {
                Log.d("QUERY_AP", "AP not found in local DB");
                // resPublic.close();
                return null;
            }
        }
    }

    private void updateResults(boolean checkWifi) {
        if (checkWifi) {
            if (Utils.checkWifiAndEnable(this, mWifiManager)) {
                updateResultsInternal();
            }
        } else updateResultsInternal();
    }

    private void updateResultsInternal() {
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            scanAndQueryNetworks();
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

    private void scanAndQueryNetworks() {
        mAdapter.clear();

        List<ScanResult> results = mWifiManager.getScanResults();
        results = mWifiManager.getScanResults();

        String message = null;
        ArrayList<QueryResult> foundAps = new ArrayList<QueryResult>();

        if (results != null) {
            if (results.size() == 0) {
                message = getString(R.string.scan_none_in_range);
            } else {
                for (ScanResult result : results) {
                    Log.d("WIFI_RESULT", result.BSSID + "\t" + result.SSID + "\t" + result.level + "\n" + result.capabilities);
                    QueryResult qret = queryNetwork(result.BSSID, result.SSID, result.level);
                    if (qret != null) {
                        foundAps.add(qret);
                    }
                }
                if (foundAps.size() == 0) {
                    message = getString(R.string.scan_not_found_range);
                }
            }
        } else {
            message = getString(R.string.scan_no_results);
        }
        if (message != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            setEmptyText(getString(R.string.scan_no_match));
        }
        mAdapter.addAll(foundAps);
    }

    @Override
    public void onClick(View v) {
        updateResults(true);
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onFinishWifiStateDialog() {
        mWifiManager.setWifiEnabled(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateResults(false);
            }
        }, 1000);
    }

    public class QueryResult implements Comparable<QueryResult> {
        public AccessPoint mAccessPoint;
        public String mScanBssid;
        public String mScanName;
        public boolean isBssidDifferent = true;
        public boolean isNameDifferent = true;
        public int mScanLevel;

        public QueryResult(String scanBssid, String scanName, int scanLevel, AccessPoint ap) {
            this.mAccessPoint = ap;
            this.mScanLevel = scanLevel;
            this.mScanBssid = scanBssid;
            this.mScanName = scanName;
            if (ap != null) {
                this.isBssidDifferent = !mAccessPoint.getBssid().equals(mScanBssid);
                this.isNameDifferent = !mAccessPoint.getName().equals(mScanName);
            }
        }

        @Override
        public int compareTo(QueryResult another) {
            if (another != null) {
                if (this.mScanLevel > another.mScanLevel) {
                    return -1;
                } else if (this.mScanLevel < another.mScanLevel) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        }
    }
}
