package com.smile.colorballs;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.smile.smilepublicclasseslibrary.facebook_ads_util.*;
import com.smile.smilepublicclasseslibrary.google_admob_ads_util.*;
import com.smile.smilepublicclasseslibrary.scoresqlite.*;
import com.smile.smilepublicclasseslibrary.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

import java.util.HashMap;

public class ColorBallsApp extends Application {

    // public static final String REST_Website = new String("http://192.168.0.10:5000/Playerscore");
    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final int GameId = 1; // this GameId is for backend game_id in playerscore table

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;

    // 10->RED, 20->GREEN, 30->BLUE, 40->MAGENTA, 50->YELLOW, 60->Cyan
    public static final int ColorRED = 10;
    public static final int ColorGREEN = 20;
    public static final int ColorBLUE = 30;
    public static final int ColorMAGENTA = 40;
    public static final int ColorYELLOW = 50;
    public static final int ColorCyan = 60;
    public static final int MaxBalls = 6;   // 6 colors totally
    public static final int[] ballColor = new int[] {ColorRED, ColorGREEN, ColorBLUE, ColorMAGENTA, ColorYELLOW, ColorCyan};

    public static HashMap<Integer, Bitmap> colorBallMap;
    public static HashMap<Integer, Bitmap> colorOvalBallMap;

    public static boolean isProcessingJob;
    public static boolean isShowingLoadingMessage;
    public static boolean isShowingSavingGameMessage;
    public static boolean isShowingLoadingGameMessage;

    public static ShowingInterstitialAdsUtil InterstitialAd;
    public static String googleAdMobBannerID = "";

    private static FacebookInterstitialAds facebookAds;
    private static GoogleAdMobInterstitial googleInterstitialAd;

    @Override
    public void onCreate() {
        super.onCreate();

        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(AppContext);

        colorBallMap = new HashMap<>();
        colorOvalBallMap = new HashMap<>();

        Bitmap bm = BitmapFactory.decodeResource(AppResources, R.drawable.redball);
        colorBallMap.put(ColorRED, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.redball_o);
        colorOvalBallMap.put(ColorRED, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.greenball);
        colorBallMap.put(ColorGREEN, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.greenball_o);
        colorOvalBallMap.put(ColorGREEN, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.blueball);
        colorBallMap.put(ColorBLUE, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.blueball_o);
        colorOvalBallMap.put(ColorBLUE, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.magentaball);
        colorBallMap.put(ColorMAGENTA, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.magentaball_o);
        colorOvalBallMap.put(ColorMAGENTA, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.yellowball);
        colorBallMap.put(ColorYELLOW, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.yellowball_o);
        colorOvalBallMap.put(ColorYELLOW, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.cyanball);
        colorBallMap.put(ColorCyan, bm);
        bm = BitmapFactory.decodeResource(AppResources, R.drawable.cyanball_o);
        colorOvalBallMap.put(ColorCyan, bm);

        isProcessingJob = false;
        isShowingLoadingMessage = false;
        isShowingSavingGameMessage = false;
        isShowingLoadingGameMessage = false;

        String facebookPlacementID = new String("200699663911258_200701030577788"); // for colorballs
        String googleAdMobAppID = getString(R.string.google_AdMobAppID);
        String googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1276882569";
        if (BuildConfig.APPLICATION_ID == "com.smile.colorballs") {
            facebookPlacementID = new String("200699663911258_200701030577788"); // for colorballs
            // Google AdMob
            googleAdMobAppID = getString(R.string.google_AdMobAppID);
            googleAdMobBannerID = "ca-app-pub-8354869049759576/3904969730";
            googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1276882569";
        } else if (BuildConfig.APPLICATION_ID == "com.smile.fivecolorballs") {
            facebookPlacementID = new String("241884113266033_241884616599316"); // for fivecolorballs
            // Google AdMob
            googleAdMobAppID = getString(R.string.google_AdMobAppID_2);
            googleAdMobInterstitialID = "ca-app-pub-8354869049759576/2174745857";
            googleAdMobBannerID = "ca-app-pub-8354869049759576/7162646323";
        } else {
            // default
        }

        System.out.println("BuildConfig.APPLICATION_ID = " + BuildConfig.APPLICATION_ID);
        facebookAds = new FacebookInterstitialAds(ColorBallsApp.AppContext, facebookPlacementID);
        facebookAds.loadAd();

        MobileAds.initialize(AppContext, googleAdMobAppID);
        googleInterstitialAd = new GoogleAdMobInterstitial(AppContext, googleAdMobInterstitialID);
        googleInterstitialAd.loadAd(); // load first ad

        InterstitialAd = new ShowingInterstitialAdsUtil(facebookAds, googleInterstitialAd);
    }
}
