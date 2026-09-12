package com.smile.colorballs

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class ColorBallsApp : BaseApp() {

    companion object {
        private const val META_BANNER_ID = "200699663911258_423008208347068"
        private const val META_BANNER_ID2 = "200699663911258_619846328663254"
        private const val META_INTERSTITIAL_ID = "200699663911258_200701030577788" // for colorballs
        // Google AdMob
        private const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/3904969730"
        private const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/9583367128"
        private const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/2356386907"
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
        // val admobInterstitialID = "ca-app-pub-8354869049759576/1276882569"
        // return AdMobInterstitial(applicationContext, admobInterstitialID)
        return null
    }

    override fun getFacebookBannerID(): String {
        return META_BANNER_ID
    }

    override fun getFacebookBannerID2(): String {
        return META_BANNER_ID2
    }
}