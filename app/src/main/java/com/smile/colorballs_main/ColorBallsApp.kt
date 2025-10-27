package com.smile.colorballs_main

import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.smile.colorballs_main.tools.LogUtil
// import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitial
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class ColorBallsApp : MultiDexApplication() {
    // var facebookAds: FacebookInterstitial? = null
    var adMobInterstitial: AdMobInterstitial? = null
    override fun onCreate() {
        super.onCreate()
        /*
        // no needed when using AdMob mediation
        if (!AudienceNetworkAds.isInitialized(this)) {
            if (BuildConfig.DEBUG) {
                AdSettings.turnOnSDKDebugger(this)
            }
            AudienceNetworkAds
                .buildInitSettings(this)
                .withInitListener { initResult: InitResult ->
                    LogUtil.d(TAG,initResult.message)
                }
                .initialize()
        }
        */
        
        /*
        var testString = ""
        // for debug mode
        if (BuildConfig.DEBUG) {
            testString = "IMG_16_9_APP_INSTALL#"
        }
        val fInterstitialId = testString + META_INTERSTITIAL_ID
        facebookAds = FacebookInterstitial(applicationContext, fInterstitialId)
        */
        MobileAds.initialize(
            applicationContext
        ) {
            LogUtil.d(TAG, "AdMob ads was initialized successfully.")
        }
        // AdSettings.addTestDevice("f9608db1-8051-497c-a399-e9ecb6c35707")  // Samsung A10 S
        adMobInterstitial = AdMobInterstitial(applicationContext, ADMOB_INTERSTITIAL_ID)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LogUtil.i(TAG, "onTrimMemory.level = $level")
    }

    companion object {
        private const val TAG = "ColorBallsApp"
        var textFontSize: Float = 0f
        const val META_BANNER_ID = "200699663911258_423008208347068"
        const val META_BANNER_ID2 = "200699663911258_619846328663254"
        const val META_INTERSTITIAL_ID = "200699663911258_200701030577788" // for colorballs
        // Google AdMob
        const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/3904969730"
        const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/9583367128"
        const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/2356386907"
        const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-8354869049759576/1276882569"
    }
}