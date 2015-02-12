package com.titantech.wifibuddy.controllers;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.titantech.wifibuddy.R;

public class WifiStateDialog extends DialogFragment {
    public interface WifiStateDialogListener {
        void onFinishWifiStateDialog();
    }

    public WifiStateDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_wifi_state, container);
        Button mButtonOk = (Button) view.findViewById(R.id.dialog_wifi_ok);
        Button mButtonCancel = (Button) view.findViewById(R.id.dialog_wifi_cancel);

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dismiss();
                    WifiStateDialogListener activity = (WifiStateDialogListener) getTargetFragment();
                    activity.onFinishWifiStateDialog();
                } catch (ClassCastException ex) {
                    Log.e("WIFI_DIALOG", "Class cannot be cast to WifiStateDialogListener");
                }
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }
}
