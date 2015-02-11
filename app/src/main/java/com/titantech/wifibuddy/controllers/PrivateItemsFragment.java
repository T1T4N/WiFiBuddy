package com.titantech.wifibuddy.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.shamanland.fab.FloatingActionButton;
import com.shamanland.fab.ShowHideOnScroll;
import com.titantech.wifibuddy.MainActivity;
import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.adapters.PrivateItemsAdapter;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.AccessPoint;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.provider.WifiContentProvider;
import com.titantech.wifibuddy.service.IntentFactory;

import java.util.HashMap;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PrivateItemsFragment extends Fragment
    implements AdapterView.OnItemLongClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentInteractionListener mListener;
    private SectionChangedListener mSectionChangedListener;
    private HashMap<Integer, String> mSectionTitles;
    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListViewPrivate;
    private SwipeDismissListViewTouchListener mSwipeDismissListener;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CursorAdapter mAdapterPrivate;

    // TODO: Rename and change types of parameters
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

    @SuppressLint("UseSparseArrays")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private, container, false);

        // Set the adapter
        mListViewPrivate = (ListView) view.findViewById(R.id.list_private);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.private_fab);
        mListViewPrivate.setEmptyView(view.findViewById(android.R.id.empty));
        mSectionTitles = new HashMap<Integer, String>();
        mSectionTitles.put(0, getString(R.string.header_public));
        mSectionTitles.put(1, getString(R.string.header_private));

        try {
            initLoader();
            mAdapterPrivate = new PrivateItemsAdapter(getActivity(), null, mSectionTitles);
            mListViewPrivate.setAdapter(mAdapterPrivate);

            mSwipeDismissListener = new SwipeDismissListViewTouchListener(
                mListViewPrivate, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                @Override
                public boolean canDismiss(int position) {
                    return true;
                }

                @Override
                public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                    for (int position : reverseSortedPositions) {
                        Toast.makeText(getActivity(), "Remove " + String.valueOf(position), Toast.LENGTH_SHORT).show();
                        Object a = mAdapterPrivate.getItem(position);

                        //mAdapterPrivate.remove(adapter.getItem(position));
                    }
                    // mAdapterPrivate.notifyDataSetChanged();
                }
            }
            );
            mListViewPrivate.setOnTouchListener(mSwipeDismissListener);
            mListViewPrivate.setOnScrollListener(mSwipeDismissListener.makeScrollListener());

            Intent intent = IntentFactory.getPrivateItems(getActivity());
            getActivity().startService(intent);

            mListViewPrivate.setOnItemLongClickListener(this);
        } catch (Exception ex) {
            Log.e("CURSOR_ERROR", "Error onCreateView");
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
        mListener = null;
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
            Log.e("CURSOR_ERROR", "Error onCreateLoader");
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            mAdapterPrivate.swapCursor(data);
        } catch (Exception ex) {
            Log.e("CURSOR_ERROR", "Error onLoadFinished");
            ex.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        try {
            mAdapterPrivate.swapCursor(null);
        } catch (Exception ex) {
            Log.e("CURSOR_ERROR", "Error onLoaderReset");
            ex.printStackTrace();
        }
    }

    private void initLoader() {
        try {
            getLoaderManager().initLoader(Constants.LOADER_PRIVATE_ID, null, this);
        } catch (Exception ex) {
            Log.e("CURSOR_ERROR", "Error initLoader");
            ex.printStackTrace();
        }
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
        intent.putExtra(Constants.EXTRA_ACTION_EDIT, ap);
        startActivity(intent);
        return true;
    }
}
