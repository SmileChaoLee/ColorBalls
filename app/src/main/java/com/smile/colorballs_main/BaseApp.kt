package com.smile.colorballs_main

import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.MobileAds
import com.smile.colorballs_main.tools.LogUtil
// import com.smile.smilelibraries.facebook_ads_util.FacebookInterstitial
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

abstract class BaseApp : MultiDexApplication() {

    companion object {
        private const val TAG = "BaseApp"
    }

    abstract fun getBannerID(): String
    abstract fun getBannerID2(): String
    abstract fun getNativeID(): String
    abstract fun getInterstitial(): AdMobInterstitial?

    // var facebookAds: FacebookInterstitial? = null
    override fun onCreate() {
        super.onCreate()
        LogUtil.i(TAG, "onCreate")
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
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LogUtil.i(TAG, "onTrimMemory.level = $level")
    }
}