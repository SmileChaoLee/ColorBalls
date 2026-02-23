package com.smile.fivecolorballs

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class FiveCBallsApp : BaseApp() {

    companion object {
        const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/7162646323"
        const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/5784271650"
        const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/8621863614"
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

    override fun getInterstitial(): AdMobInterstitial? {
        val admobInterstitialID = "ca-app-pub-8354869049759576/2174745857"
        // no interstitial ad for now
        // return AdMobInterstitial(applicationContext, admobInterstitialID)
        return null
    }
}