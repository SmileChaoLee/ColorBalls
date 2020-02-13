package com.smile.colorballs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.multidex.MultiDexApplication;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitialAds;
import com.smile.smilelibraries.google_admob_ads_util.GoogleAdMobInterstitial;
import com.smile.smilelibraries.scoresqlite.ScoreSQLite;
import com.smile.smilelibraries.showing_instertitial_ads_utility.ShowingInterstitialAdsUtil;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.HashMap;

public class ColorBallsApp extends MultiDexApplication {

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
    public static final int NumOfColorsUsedByEasy = 5;          // 5 colors for easy level
    public static final int NumOfColorsUsedByDifficult = 6;    // 6 colors for difficult level
    public static final int[] ballColor = new int[] {ColorRED, ColorGREEN, ColorBLUE, ColorMAGENTA, ColorYELLOW, ColorCyan};
    public static final int FontSize_Scale_Type = ScreenUtil.FontSize_Pixel_Type;
    public static final int Max_Saved_Games = 5;
    public static final String NumOfSavedGameFileName = "num_saved_game";
    public static final String PrivacyPolicyUrl = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/PrivacyPolicy";

    public static HashMap<Integer, Bitmap> colorBallMap;
    public static HashMap<Integer, Bitmap> colorOvalBallMap;

    public static boolean isProcessingJob;
    public static boolean isShowingLoadingMessage;
    public static boolean isShowingSavingGameMessage;
    public static boolean isShowingLoadingGameMessage;
    public static boolean isProVersion = false;

    public static ShowingInterstitialAdsUtil InterstitialAd;
    public static String facebookBannerID = "";
    public static String googleAdMobBannerID = "";
    // public static int AdProvider = ShowingInterstitialAdsUtil.GoogleAdMobAdProvider;    // default is Google AdMob
    public static int AdProvider = ShowingInterstitialAdsUtil.FacebookAdProvider;    // default is Facebook Ad

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

        String facebookInterstitialID = ""; // for colorballs
        facebookBannerID = "";
        String googleAdMobAppID = "";
        googleAdMobBannerID = "";
        String googleAdMobInterstitialID = "";
        isProVersion = false;
        if (BuildConfig.APPLICATION_ID.equals("com.smile.colorballs")) {
            facebookInterstitialID = new String("200699663911258_200701030577788"); // for colorballs
            facebookBannerID = "200699663911258_423008208347068";
            // Google AdMob
            googleAdMobAppID = getString(R.string.google_AdMobAppID);
            googleAdMobBannerID = "ca-app-pub-8354869049759576/3904969730";
            googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1276882569";
        } else if (BuildConfig.APPLICATION_ID.equals("com.smile.fivecolorballs")) {
            facebookInterstitialID = new String("241884113266033_241884616599316"); // for fivecolorballs
            facebookBannerID = "241884113266033_515925465861895";   // 241884113266033_515925465861895
            // Google AdMob
            googleAdMobAppID = getString(R.string.google_AdMobAppID_2);
            googleAdMobInterstitialID = "ca-app-pub-8354869049759576/2174745857";
            googleAdMobBannerID = "ca-app-pub-8354869049759576/7162646323";
        } else {
            // default for professional version
            isProVersion = true;
        }

        if (!isProVersion) {
            AudienceNetworkAds.initialize(this);
            facebookAds = new FacebookInterstitialAds(ColorBallsApp.AppContext, facebookInterstitialID);

            MobileAds.initialize(AppContext, googleAdMobAppID);
            googleInterstitialAd = new GoogleAdMobInterstitial(AppContext, googleAdMobInterstitialID);

            InterstitialAd = new ShowingInterstitialAdsUtil(facebookAds, googleInterstitialAd);

            final Handler adHandler = new Handler(Looper.getMainLooper());
            final Runnable adRunnable = new Runnable() {
                @Override
                public void run() {
                    adHandler.removeCallbacksAndMessages(null);
                    googleInterstitialAd.loadAd(); // load first google ad
                    facebookAds.loadAd();   // load first facebook ad
                }
            };
            adHandler.postDelayed(adRunnable, 1000);
        }
    }
}
