package com.smile.reversi

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class ReversiApp : BaseApp() {

    companion object {
        // Google AdMob
        private const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/4457337489"
        private const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/5988131333"
        private const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/6305871708"
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