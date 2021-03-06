package com.smile.colorballs;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitialAds;
import com.smile.smilelibraries.google_admob_ads_util.GoogleAdMobInterstitial;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;
import com.smile.smilelibraries.showing_interstitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import static com.smile.colorballs.BuildConfig.DEBUG;

public class ColorBallsApp extends MultiDexApplication {

    private static final String TAG = "ColorBallsApp";

    public static final String REST_Website = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/Playerscore";
    public static final int GameId = 1; // this GameId is for backend game_id in playerscore table
    public static final int FontSize_Scale_Type = ScreenUtil.FontSize_Pixel_Type;

    public static Resources AppResources;
    public static Context AppContext;
    public static ScoreSQLite ScoreSQLiteDB;

    public static boolean isProcessingJob;
    public static boolean isShowingLoadingMessage;
    public static boolean isProVersion = false;

    public static ShowingInterstitialAdsUtil InterstitialAd;
    public static String facebookBannerID = "";
    public static String facebookBannerID2 = "";
    public static String googleAdMobBannerID = "";
    public static String googleAdMobBannerID2 = "";
    public static String googleAdMobNativeID = "";
    public static int AdProvider = ShowingInterstitialAdsUtil.FacebookAdProvider;    // default is Facebook Ad
    public static FacebookInterstitialAds facebookAds;
    public static GoogleAdMobInterstitial googleInterstitialAd;

    @Override
    public void onCreate() {
        super.onCreate();

        AppResources = getResources();
        AppContext = getApplicationContext();
        ScoreSQLiteDB = new ScoreSQLite(AppContext);

        isProcessingJob = false;
        isShowingLoadingMessage = false;

        String facebookInterstitialID = ""; // for colorballs
        facebookBannerID = "";
        facebookBannerID2 = "";
        String googleAdMobAppID = "";
        googleAdMobBannerID = "";
        googleAdMobBannerID2 = "";
        googleAdMobNativeID = "";
        String googleAdMobInterstitialID = "";
        isProVersion = false;
        if (BuildConfig.APPLICATION_ID.equals("com.smile.colorballs")) {
            facebookInterstitialID = new String("200699663911258_200701030577788"); // for colorballs
            facebookBannerID = "200699663911258_423008208347068";
            facebookBannerID2 = "200699663911258_619846328663254";
            // Google AdMob
            googleAdMobAppID = getString(R.string.google_AdMobAppID);
            googleAdMobBannerID = "ca-app-pub-8354869049759576/3904969730";
            googleAdMobBannerID2 = "ca-app-pub-8354869049759576/9583367128";
            googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1276882569";
            googleAdMobNativeID = "ca-app-pub-8354869049759576/2356386907";
        } else if (BuildConfig.APPLICATION_ID.equals("com.smile.fivecolorballs")) {
            facebookInterstitialID = new String("241884113266033_241884616599316"); // for fivecolorballs
            facebookBannerID = "241884113266033_515925465861895";   // 241884113266033_515925465861895
            facebookBannerID2 = "241884113266033_749644445823328";
            // Google AdMob
            googleAdMobAppID = getString(R.string.google_AdMobAppID_2);
            googleAdMobBannerID = "ca-app-pub-8354869049759576/7162646323";
            googleAdMobBannerID2 = "ca-app-pub-8354869049759576/5784271650";
            googleAdMobInterstitialID = "ca-app-pub-8354869049759576/2174745857";
            googleAdMobNativeID = "ca-app-pub-8354869049759576/8621863614";
        } else {
            // default for professional version
            isProVersion = true;
        }

        if (!isProVersion) {
            if (!AudienceNetworkAds.isInitialized(this)) {
                if (DEBUG) {
                    AdSettings.turnOnSDKDebugger(this);
                }
                AudienceNetworkAds
                        .buildInitSettings(this)
                        .withInitListener(new  AudienceNetworkAds.InitListener() {
                            @Override
                            public void onInitialized(AudienceNetworkAds.InitResult initResult) {
                                Log.d(AudienceNetworkAds.TAG, initResult.getMessage());
                            }
                        })
                        .initialize();
            }
            // AudienceNetworkAds.initialize(this);
            String testString = "";
            // for debug mode
            if (DEBUG) {
                testString = "IMG_16_9_APP_INSTALL#";
            }
            facebookInterstitialID = testString + facebookInterstitialID;
            //
            facebookAds = new FacebookInterstitialAds(ColorBallsApp.AppContext, facebookInterstitialID);

            MobileAds.initialize(AppContext, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Log.d(TAG, "Google AdMob was initialized successfully.");
                }

            });
            googleInterstitialAd = new GoogleAdMobInterstitial(AppContext, googleAdMobInterstitialID);

            // Moved to MyActivity.class on 2020-06-15 because need to convert context to activity
            // InterstitialAd = new ShowingInterstitialAdsUtil(AppContext, facebookAds, googleInterstitialAd);

            final Handler adHandler = new Handler(Looper.getMainLooper());
            final Runnable adRunnable = new Runnable() {
                @Override
                public void run() {
                    adHandler.removeCallbacksAndMessages(null);
                    if (googleInterstitialAd != null) {
                        googleInterstitialAd.loadAd(); // load first google ad
                    }
                    if (facebookAds != null) {
                        facebookAds.loadAd();   // load first facebook ad
                    }
                }
            };
            adHandler.postDelayed(adRunnable, 1000);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory() is called.");
    }
}
