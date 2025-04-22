package com.smile.colorballs

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.AudienceNetworkAds.InitResult
import com.google.android.gms.ads.MobileAds
import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitial
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class ColorBallsApp : MultiDexApplication() {
    @JvmField
    var facebookAds: FacebookInterstitial? = null
    @JvmField
    var googleInterstitialAd: AdMobInterstitial? = null
    override fun onCreate() {
        super.onCreate()
        isProcessingJob = false
        isShowingLoadingMessage = false
        val googleAdMobInterstitialID = "ca-app-pub-8354869049759576/1276882569"
        var facebookInterstitialID = "200699663911258_200701030577788" // for colorballs
        facebookBannerID = "200699663911258_423008208347068"
        facebookBannerID2 = "200699663911258_619846328663254"
        // Google AdMob
        googleAdMobBannerID = "ca-app-pub-8354869049759576/3904969730"
        googleAdMobBannerID2 = "ca-app-pub-8354869049759576/9583367128"
        googleAdMobNativeID = "ca-app-pub-8354869049759576/2356386907"
        if (!AudienceNetworkAds.isInitialized(this)) {
            if (BuildConfig.DEBUG) {
                AdSettings.turnOnSDKDebugger(this)
            }
            AudienceNetworkAds
                .buildInitSettings(this)
                .withInitListener { initResult: InitResult ->
                    Log.d(
                        AudienceNetworkAds.TAG,
                        initResult.message
                    )
                }
                .initialize()
        }
        // AudienceNetworkAds.initialize(this);
        var testString = ""
        // for debug mode
        if (BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#"
        }
        facebookInterstitialID = testString + facebookInterstitialID
        //
        facebookAds = FacebookInterstitial(applicationContext, facebookInterstitialID)
        MobileAds.initialize(
            applicationContext
        ) {
            Log.d(
                TAG, "Google AdMob was initialized successfully."
            )
        }
        googleInterstitialAd = AdMobInterstitial(applicationContext, googleAdMobInterstitialID)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory.level = $level")
    }

    companion object {
        private const val TAG = "ColorBallsApp"
        @JvmField
        var isProcessingJob = false
        @JvmField
        var isShowingLoadingMessage = false
        @JvmField
        var facebookBannerID = ""
        @JvmField
        var facebookBannerID2 = ""
        @JvmField
        var googleAdMobBannerID = ""
        @JvmField
        var googleAdMobBannerID2 = ""
        @JvmField
        var googleAdMobNativeID = ""
    }
}