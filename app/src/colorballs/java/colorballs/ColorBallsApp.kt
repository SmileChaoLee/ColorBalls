package colorballs

import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial

class ColorBallsApp : BaseApp() {

    companion object {
        const val META_BANNER_ID = "200699663911258_423008208347068"
        const val META_BANNER_ID2 = "200699663911258_619846328663254"
        const val META_INTERSTITIAL_ID = "200699663911258_200701030577788" // for colorballs
        // Google AdMob
        const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/3904969730"
        const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/9583367128"
        const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/2356386907"
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
        val admobInterstitialID = "ca-app-pub-8354869049759576/1276882569"
        return AdMobInterstitial(applicationContext, admobInterstitialID)
    }
}