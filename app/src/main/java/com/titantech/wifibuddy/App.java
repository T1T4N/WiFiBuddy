package com.titantech.wifibuddy;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "Oi0elRV8JuenPWX7XT1v3RXlNFxC4plNweaqEmG5", "LsW9Il29jMvaktO2PQ3bVS7bUyoFfhZBN6LjzTBN");
    }
}
