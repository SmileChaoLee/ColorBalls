package com.smile.smilelibraries

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.ads.*

class FacebookBanner(private val adView: AdView) {

    companion object {
        private const val TAG = "FacebookBanner"
        private const val MAX_LOAD_NUM = 15
        private const val ONE_MINUTE = 60000L    // 60 seconds
        private const val TWO_MINUTES = 120000L // 120 seconds
        private const val TEN_MINUTES = 600000L // 600 seconds, 10 minutes
    }

    private val adId = adView.placementId
    private var adListener: AdListener? = null
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

    constructor(context: Context, adId: String)
            : this(AdView(context, adId, AdSize.BANNER_HEIGHT_50))

    init {
        adListener = object : AdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
                Log.d(TAG,"onError.adId = $adId")
                Log.d(TAG,"onError.numberOfLoad = $numberOfLoad")
                isAdLoaded = false
                timerHandler.removeCallbacksAndMessages(null)
                if (numberOfLoad < MAX_LOAD_NUM) {
                    numberOfLoad++
                    timerHandler.postDelayed(timerRunnable, TWO_MINUTES)
                } else {
                    numberOfLoad = 0 // set back to zero
                    Log.d(TAG,"onError.more than $MAX_LOAD_NUM")
                    timerHandler.postDelayed(timerRunnable, TEN_MINUTES)
                }
            }
            override fun onAdLoaded(p0: Ad?) {
                Log.d(TAG, "onAdLoaded.adId = $adId")
                isAdLoaded = true
                numberOfLoad = 0
            }
            override fun onAdClicked(p0: Ad?) {
                Log.d(TAG, "onAdClicked.adId = $adId")
            }
            override fun onLoggingImpression(p0: Ad?) {
                Log.d(TAG, "onLoggingImpression.adId = $adId")
            }
        }
        timerHandler.post(timerRunnable) // starting load banner ad
        Log.d(TAG, "FacebookBanner.for $adId created")
    }

    private fun loadOneAd() {
        Log.d(TAG, "loadOneAd.adId = $adId")
        Log.d(TAG, "loadOneAd.numberOfLoad = $numberOfLoad")
        adListener?.let { listener ->
            adView.apply {
                loadAd(
                    buildLoadAdConfig()
                        .withAdListener(listener).build()
                )
            }
        }
    }
}