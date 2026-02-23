package com.smile.dropcolorballs

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class DropCBallsApp : BaseApp() {

    companion object {
        const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/1330279606"
        const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/6948495847"
        const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/7704116261"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun getBannerID(): String {
        return ADMOB_BANNER_ID
    }

    override fun getBannerID2(): String {
        return ADMOB_BANNER_ID2
    }

    override fun getNativeID(): String {
        return ADMOB_NATIVE_ID
    }

    override fun getInterstitial(): AdMobInterstitial {
        val admobInterstitialID = "ca-app-pub-8354869049759576/4465092297"
        return AdMobInterstitial(applicationContext, admobInterstitialID)
    }
}