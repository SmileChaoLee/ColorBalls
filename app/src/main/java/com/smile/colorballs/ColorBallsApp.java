package com.smile.colorballs;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class ColorBallsApp extends Application {

    public static Resources AppResources;
    public static Context AppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        AppResources = getResources();
        AppContext = getApplicationContext();
    }
}
