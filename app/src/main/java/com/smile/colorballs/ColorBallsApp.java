package com.smile.colorballs;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.smile.smilepublicclasseslibrary.facebookadsutil.*;
import com.smile.smilepublicclasseslibrary.scoresqlite.*;

public class ColorBallsApp extends Application {

    // public final String REST_Website = new String("http://192.168.0.11:5000/Playerscore");
    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final int GameId = 1; // this GameId is for backend game_id in playerscore table

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;
    public static FacebookInterstitialAds FacebookAds;

    @Override
    public void onCreate() {
        super.onCreate();
        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(AppContext);
        String facebookPlacementID = new String("200699663911258_200701030577788"); // for colorballs
        if (BuildConfig.APPLICATION_ID == "com.smile.colorballs") {
            facebookPlacementID = new String("200699663911258_200701030577788"); // for colorballs
        } else if (BuildConfig.APPLICATION_ID == "com.smile.fivecolorballs") {
            facebookPlacementID = new String("241884113266033_241884616599316"); // for fivecolorballs
        } else {
            // default
        }
        System.out.println("BuildConfig.APPLICATION_ID = " + BuildConfig.APPLICATION_ID);
        FacebookAds = new FacebookInterstitialAds(ColorBallsApp.AppContext, facebookPlacementID);
    }
}
