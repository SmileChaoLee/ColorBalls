package com.smile.ballsremover

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class BallsRemApp : BaseApp() {

    companion object {
        private const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/7152164841"
        private const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/7343399477"
        private const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/3429645905"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun getAdMobBannerID(): String {
        return ADMOB_BANNER_ID
    }

    override fun getAdMobBannerID2(): String {
        return ADMOB_BANNER_ID2
    }

    override fun getAdMobNativeID(): String {
        return ADMOB_NATIVE_ID
    }

    override fun getAdMobInterstitial(): AdMobInterstitial? {
        val admobInterstitialID = "ca-app-pub-8354869049759576/6690798717"
        // not interstitial ad for now
        // return AdMobInterstitial(applicationContext, admobInterstitialID)
        return null
    }

    override fun getFacebookBannerID(): String {
        return ""
    }

    override fun getFacebookBannerID2(): String {
        return ""
    }
}