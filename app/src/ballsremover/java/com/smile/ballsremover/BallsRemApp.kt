package com.smile.ballsremover

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class BallsRemApp : BaseApp() {

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
        val admobInterstitialID = "ca-app-pub-8354869049759576/6690798717"
        // not interstitial ad for now
        // return AdMobInterstitial(applicationContext, admobInterstitialID)
        return null
    }

    companion object {
        // const val META_BANNER_ID = "200699663911258_423008208347068"
        // const val META_BANNER_ID2 = "200699663911258_619846328663254"
        // const val META_INTERSTITIAL_ID = "200699663911258_200701030577788" // for colorballs
        // Google AdMob
        const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/7152164841"
        const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/7343399477"
        const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/3429645905"
    }
}