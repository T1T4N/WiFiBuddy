package com.titantech.wifibuddy.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.adapters.PublicItemsAdapter;
import com.titantech.wifibuddy.controllers.listeners.OnFragmentInteractionListener;
import com.titantech.wifibuddy.controllers.listeners.SectionChangedListener;
import com.titantech.wifibuddy.db.WifiDbOpenHelper;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.provider.WifiContentProvider;
import com.titantech.wifibuddy.service.IntentFactory;

/**
 * Created by Robert on 25.01.2015.
 */
public class PublicItemsFragment extends Fragment
    implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentInteractionListener mListener;
    private SectionChangedListener mSectionChangedListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AbsListView mListView;
    private CursorAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static PublicItemsFragment newInstance(int position) {
        PublicItemsFragment fragment = new PublicItemsFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    public PublicItemsFragment() {
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
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        // TODO: Setup swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container_public);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Refreshing done", Toast.LENGTH_LONG).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2500);

            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_red_light, R.color.colorPrimaryDark, android.R.color.holo_blue_bright
        );

        Bundle args = null;
        getLoaderManager().initLoader(Constants.LOADER_PUBLIC_ID, args, this);
        mAdapter = new PublicItemsAdapter(getActivity(), null);
        mListView.setAdapter(mAdapter);

        Intent intent = IntentFactory.getPublicItems(getActivity());
        getActivity().startService(intent);
        return view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //TODO: Private item OnClick
            //mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = WifiDbOpenHelper.PROJECTION_ALL_PUBLIC;
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
            WifiContentProvider.CONTENT_URI_PUBLIC, projection, null, null, WifiDbOpenHelper.COLUMN_NAME + " ASC");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
