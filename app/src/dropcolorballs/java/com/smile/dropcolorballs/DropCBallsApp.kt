package com.smile.dropcolorballs

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class DropCBallsApp : BaseApp() {

    companion object {
        private const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/1330279606"
        private const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/6948495847"
        private const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/7704116261"
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

    override fun getAdMobInterstitial(): AdMobInterstitial {
        val admobInterstitialID = "ca-app-pub-8354869049759576/4465092297"
        return AdMobInterstitial(applicationContext, admobInterstitialID)
    }

    override fun getFacebookBannerID(): String {
        return ""
    }

    override fun getFacebookBannerID2(): String {
        return ""
    }
}