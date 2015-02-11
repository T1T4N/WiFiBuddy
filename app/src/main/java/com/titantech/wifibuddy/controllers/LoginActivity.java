package com.titantech.wifibuddy.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.titantech.wifibuddy.MainActivity;
import com.titantech.wifibuddy.R;
import com.titantech.wifibuddy.models.User;
import com.titantech.wifibuddy.models.Utils;
import com.titantech.wifibuddy.network.RestTask;
import com.titantech.wifibuddy.network.ResultListener;
import com.titantech.wifibuddy.network.requests.GetRestRequest;
import com.titantech.wifibuddy.network.requests.PostRestRequest;
import com.titantech.wifibuddy.network.requests.RestRequest;
import com.titantech.wifibuddy.parsers.UserPutParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RestTask<User> mAuthTask = null;
    private String mAuthUrl, mRegisterUrl;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button btnSignIn, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuthUrl = getString(R.string.url_auth);
        mRegisterUrl = getString(R.string.url_register);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.ime_action_login || id == EditorInfo.IME_NULL) {
                    validateFields();
                    return true;
                }
                return false;
            }
        });

        btnSignIn = (Button) findViewById(R.id.button_sign_in);
        btnRegister = (Button) findViewById(R.id.button_register);
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();

                if (validateFields()) {
                    attemptRegister(email, password);
                }
            }
        });
        btnSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();

                if (validateFields()) {
                    attemptLogin(email, password);
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        /*
        // Setup toolbar in this activity
        Toolbar mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        */
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    public void attemptRegister(final String email, final String password) {
        showProgress(true);
        final Context currentContext = this;

        HashMap<String, String> postData = new HashMap<String, String>();
        postData.put("email", email);
        postData.put("password", password);

        RestRequest registerRequest = new PostRestRequest(mRegisterUrl, postData);
        mAuthTask = new RestTask<User>(this, new UserPutParser(), new ResultListener<User>() {
            @Override
            public void onDownloadResult(User result) {
                mAuthTask = null;
                showProgress(false);
                if (result.equals(User.nullUser())) {
                    mEmailView.setError(getString(R.string.error_login_connection));
                    mEmailView.requestFocus();
                } else if (result.equals(User.genericExisting())) {
                    mEmailView.setError(getString(R.string.error_login_exists));
                    mEmailView.requestFocus();
                } else if (result.equals(User.genericUnauthorized())) {
                    mEmailView.setError(getString(R.string.error_login_unknown));
                    mEmailView.requestFocus();
                } else {
                    Toast.makeText(currentContext, getString(R.string.register_success), Toast.LENGTH_LONG).show();

                    Utils.setAuthenticatedUser(currentContext,
                        result.getUserId(), result.getEmail(), password);
                    startActivity(new Intent(currentContext, MainActivity.class));
                    finish();
                }
            }
        });
        mAuthTask.execute(registerRequest);
    }

    public void attemptLogin(final String email, final String password) {
        showProgress(true);
        final Context currentContext = this;

        RestRequest authRequest = new GetRestRequest(mAuthUrl, email, password);
        mAuthTask = new RestTask<User>(this, new UserPutParser(), new ResultListener<User>() {
            @Override
            public void onDownloadResult(User result) {
                mAuthTask = null;
                showProgress(false);
                if (result.equals(User.nullUser())) {
                    mEmailView.setError(getString(R.string.error_login_connection));
                    mEmailView.requestFocus();
                } else if (result.equals(User.genericUnauthorized())) {
                    mEmailView.setError(getString(R.string.error_login_invalid));
                    mEmailView.requestFocus();
                } else {
                    Toast.makeText(currentContext, getString(R.string.login_success), Toast.LENGTH_LONG).show();

                    Utils.setAuthenticatedUser(currentContext,
                        result.getUserId(), result.getEmail(), password);
                    startActivity(new Intent(currentContext, MainActivity.class));
                    finish();
                }
            }
        });
        mAuthTask.execute(authRequest);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public boolean validateFields() {
        if (mAuthTask != null) {
            return true;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        return !cancel;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
            // Retrieve data rows for the device user's 'profile' contact.
            Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

            // Select only email addresses.
            ContactsContract.Contacts.Data.MIMETYPE +
                " = ?", new String[] {ContactsContract.CommonDataKinds.Email
            .CONTENT_ITEM_TYPE},

            // Show primary email addresses first. Note that there won't be
            // a primary email address if the user hasn't specified one.
            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
            new ArrayAdapter<String>(LoginActivity.this,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }
}
