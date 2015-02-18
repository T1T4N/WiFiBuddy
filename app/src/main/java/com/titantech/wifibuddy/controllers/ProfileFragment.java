package com.titantech.wifibuddy.controllers;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shamanland.fab.FloatingActionButton;
import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.controllers.listeners.SectionChangedListener;
import com.titantech.wifibuddy.models.Constants;
import com.titantech.wifibuddy.models.User;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.network.RestTask;
import com.titantech.wifibuddy.network.ResultListener;
import com.titantech.wifibuddy.network.requests.PutRestRequest;
import com.titantech.wifibuddy.parsers.UserResultParser;

/**
 * Created by Robert on 24.01.2015.
 */
public class ProfileFragment extends Fragment
    implements View.OnClickListener {
    private SectionChangedListener mSectionChangedListener;
    private LinearLayout mSectionEmail, mSectionPassword, mSectionRepeat1, mSectionRepeat2;
    private EditText mFieldEmail, mFieldPasswordCurrent, mFieldRepeat1, mFieldRepeat2;
    private TextView mCountPrivate, mCountPublic;
    private FloatingActionButton mFloatingButton;
    private boolean mFabClicked;

    public static ProfileFragment newInstance(int sectionNumber) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();

        // 0 Based sectionNumber
        args.putInt(Constants.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
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
        mFabClicked = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mFloatingButton = (FloatingActionButton) rootView.findViewById(R.id.profile_fab);
        mSectionEmail = (LinearLayout) rootView.findViewById(R.id.profile_layout_email);
        mSectionPassword = (LinearLayout) rootView.findViewById(R.id.profile_layout_password_current);
        mSectionRepeat1 = (LinearLayout) rootView.findViewById(R.id.profile_layout_repeat1);
        mSectionRepeat2 = (LinearLayout) rootView.findViewById(R.id.profile_layout_repeat2);

        mFieldEmail = (EditText) mSectionEmail.findViewById(R.id.profile_field_email);
        mFieldRepeat1 = (EditText) mSectionRepeat1.findViewById(R.id.profile_field_repeat1);
        mFieldRepeat2 = (EditText) mSectionRepeat2.findViewById(R.id.profile_field_repeat2);

        mFieldPasswordCurrent = (EditText) mSectionPassword.findViewById(R.id.profile_field_password_current);

        mCountPrivate = (TextView) rootView.findViewById(R.id.profile_field_count_private);
        mCountPublic = (TextView) rootView.findViewById(R.id.profile_field_count_public);

        mFloatingButton.setOnClickListener(this);
        fillFields();
        return rootView;
    }

    private void fillFields() {
        User authUser = Utils.getAuthenticatedUser();
        mFieldEmail.setText(authUser.getEmail());
        mFieldPasswordCurrent.setText(authUser.getPassword());

    }

    @Override
    public void onClick(View v) {
        if (!mFabClicked) {
            ObjectAnimator anim = ObjectAnimator.ofInt(mFloatingButton, "imageAlpha", 255, 0);
            anim.setDuration(150);
            anim.start();
            mFloatingButton.setImageResource(R.drawable.ic_done);
            ObjectAnimator anim2 = ObjectAnimator.ofInt(mFloatingButton, "imageAlpha", 0, 255);
            anim2.setDuration(150);
            anim2.start();

            mSectionRepeat1.setVisibility(View.VISIBLE);
            mSectionRepeat2.setVisibility(View.VISIBLE);

            mFieldRepeat1.setEnabled(true);
            mFieldRepeat1.setText("");
            mFieldRepeat2.setEnabled(true);
            mFieldRepeat2.setText("");

            mFieldPasswordCurrent.setText("");
            mFieldPasswordCurrent.setEnabled(true);
            mFieldPasswordCurrent.requestFocus();
            mFabClicked = !mFabClicked;
        } else {
            if (validateFields()) {
                String oldPassword = mFieldPasswordCurrent.getText().toString();
                String newPassword = mFieldRepeat1.getText().toString();

                ObjectAnimator anim = ObjectAnimator.ofInt(mFloatingButton, "imageAlpha", 255, 0);
                anim.setDuration(150);
                anim.start();
                mFloatingButton.setImageResource(R.drawable.ic_create);
                ObjectAnimator anim2 = ObjectAnimator.ofInt(mFloatingButton, "imageAlpha", 0, 255);
                anim2.setDuration(150);
                anim2.start();

                mFieldRepeat1.setEnabled(false);
                mFieldRepeat2.setEnabled(false);
                mFieldPasswordCurrent.setEnabled(false);
                mSectionRepeat2.setVisibility(View.GONE);
                mSectionRepeat1.setVisibility(View.GONE);

                if (oldPassword.length() > 0) {
                    if (Utils.isInternetAvailable(getActivity())) {
                        User authUser = Utils.getAuthenticatedUser();
                        updateServer(
                            new User(
                                authUser.getUserId(),
                                authUser.getEmail(),
                                newPassword),
                            oldPassword);
                    } else {
                        Toast.makeText(getActivity(),
                            getString(R.string.profile_error_no_connection) + "\n" +
                                getString(R.string.profile_error_password_change),
                            Toast.LENGTH_SHORT).show();
                    }
                }
                fillFields();
                mFabClicked = !mFabClicked;
            }
        }
    }

    private void updateServer(final User newData, String oldPassword) {
        PutRestRequest putRequest = new PutRestRequest(
            getString(R.string.url_auth),
            newData.getData(),
            newData.getEmail(),
            oldPassword
        );

        RestTask<User> updateTask = new RestTask<User>(getActivity(), new UserResultParser(), new ResultListener<User>() {
            @Override
            public void onDownloadResult(User result) {
                if (result.equals(User.nullUser())) {
                    Toast.makeText(getActivity(),
                        getString(R.string.profile_error_connection) + "\n" +
                            getString(R.string.profile_error_password_change),
                        Toast.LENGTH_LONG).show();


                } else if (result.equals(User.genericUnauthorized())) {
                    Toast.makeText(getActivity(),
                        getString(R.string.error_login_unknown) + "\n" +
                            getString(R.string.profile_error_password_change),
                        Toast.LENGTH_LONG).show();

                } else {
                    Utils.setAuthenticatedUser(getActivity(), newData);
                    Toast.makeText(getActivity(),
                        getString(R.string.profile_change_success),
                        Toast.LENGTH_LONG).show();
                }
            }
        });
        updateTask.execute(putRequest);
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

    public boolean validateFields() {
        mFieldPasswordCurrent.setError(null);

        // Store values at the time of the login attempt.
        String password = mFieldPasswordCurrent.getText().toString();
        String newPassword = mFieldRepeat1.getText().toString();
        String repeatPassword = mFieldRepeat2.getText().toString();

        boolean valid = true;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password)) {
            if (!isCurrentPasswordValid(password)) {
                mFieldPasswordCurrent.setError(getString(R.string.error_incorrect_password));
                focusView = mFieldPasswordCurrent;
                valid = false;
            }

            if (valid) {
                if (TextUtils.isEmpty(newPassword)) {
                    mFieldRepeat1.setError(getString(R.string.error_field_required));
                    focusView = mFieldRepeat1;
                    valid = false;
                } else if (!isNewPasswordValid(newPassword)) {
                    mFieldRepeat1.setError(getString(R.string.error_invalid_password));
                    focusView = mFieldRepeat1;
                    valid = false;
                }

                if (valid) {
                    if (TextUtils.isEmpty(repeatPassword)) {
                        mFieldRepeat2.setError(getString(R.string.error_field_required));
                        focusView = mFieldRepeat2;
                        valid = false;
                    } else if (!repeatPassword.equals(newPassword)) {
                        mFieldRepeat2.setError(getString(R.string.error_passwords_no_match));
                        focusView = mFieldRepeat2;
                        valid = false;
                    }
                }
            }
            if (!valid) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            }
        }
        return valid;
    }

    private boolean isNewPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isCurrentPasswordValid(String password) {
        return password.equals(Utils.getAuthenticatedUser().getPassword());
    }
}
