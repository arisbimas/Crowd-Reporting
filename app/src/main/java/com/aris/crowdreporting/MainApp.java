package com.aris.crowdreporting;

import android.app.Application;

import com.firebase.client.Firebase;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
    }
}
