package com.smile.colorballs;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.smile.scoresqlite.ScoreSQLite;

public class ColorBallsApp extends Application {

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;

    @Override
    public void onCreate() {
        super.onCreate();
        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(AppContext);
    }
}
