package com.smile.reversi

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class ReversiApp : BaseApp() {

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
        // return AdMobInterstitial(applicationContext, admobInterstitialID)
        return null
    }

    companion object {
        // Google AdMob
        const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/4457337489"
        const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/5988131333"
        const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/6305871708"
    }
}