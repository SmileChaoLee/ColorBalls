package com.smile.colorballs_main

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.smile.colorballs_main.tools.LogUtil
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

abstract class BaseApp : Application() {

    companion object {
        private const val TAG = "BaseApp"
    }

    abstract fun getAdMobBannerID(): String
    abstract fun getAdMobBannerID2(): String
    abstract fun getAdMobNativeID(): String
    abstract fun getAdMobInterstitial(): AdMobInterstitial?

    override fun onCreate() {
        super.onCreate()
        LogUtil.d(TAG, "onCreate")
        MobileAds.initialize(
            applicationContext
        ) {
            LogUtil.d(TAG, "AdMob ads was initialized successfully.")
        }
        // AdSettings.addTestDevice("f9608db1-8051-497c-a399-e9ecb6c35707")  // Samsung A10 S
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LogUtil.d(TAG, "onTrimMemory.level = $level")
    }
}