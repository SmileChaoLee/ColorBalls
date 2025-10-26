package com.smile.smilelibraries

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdSize.*

class AdMobBanner(private val mAdView: AdView,
                  private val adId: String,
                  private val bannerWidth: Int = 0) {

    companion object {
        private const val TAG = "AdMobBanner"
        private const val MAX_LOAD_NUM = 15
        private const val ONE_MINUTE = 60000L    // 60 seconds
        private const val TWO_MINUTES = 120000L // 120 seconds
        private const val TEN_MINUTES = 600000L    // 600 seconds
    }

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            timerHandler.removeCallbacksAndMessages(null)
            loadOneAd()
            timerHandler.postDelayed(this, TEN_MINUTES)
        }
    }
    private var isAdLoaded = false
    private var numberOfLoad = 0

    constructor(context: Context, adId: String, bannerWidth: Int = 0)
            : this(AdView(context), adId, bannerWidth)

    init {
        mAdView.apply {
            val adMobAdSize = if (bannerWidth != 0) // adaptive banner
                getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, bannerWidth)
            else AdSize.BANNER // normal banner

            val listener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    isAdLoaded = false
                    Log.d(TAG, "onAdFailedToLoad.adId = $adId")
                    Log.d(TAG, "onAdFailedToLoad.numberOfLoad = $numberOfLoad")
                    timerHandler.removeCallbacksAndMessages(null)
                    if (numberOfLoad < MAX_LOAD_NUM) {
                        numberOfLoad++
                        timerHandler.postDelayed(timerRunnable, TWO_MINUTES)
                    } else {
                        numberOfLoad = 0 // set back to zero
                        Log.d(TAG,"onAdFailedToLoad.more than $MAX_LOAD_NUM")
                        timerHandler.postDelayed(timerRunnable, TEN_MINUTES)
                    }
                }
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "onAdLoaded.adId = $adId")
                    isAdLoaded = true
                    numberOfLoad = 0    // set to 0
                }
            }
            setAdSize(adMobAdSize)
            adUnitId = adId
            adListener = listener
        }

        loadOneAd() // use AdMob automatically reload after optimized time
        // timerHandler.post(timerRunnable)
        Log.d(TAG, "AdBoMbBanner for $adId created")
    }

    private fun loadOneAd() {
        Log.d(TAG, "loadOneAd.adId = $adId")
        Log.d(TAG, "loadOneAd.numberOfLoad = $numberOfLoad")
        mAdView.loadAd(AdRequest.Builder().build())
    }
}