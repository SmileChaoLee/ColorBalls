package com.smile

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.smile.nativetemplates_models.GoogleAdMobNativeTemplate

abstract class GoogleNativeAd(
    private val context: Context,
    private val adId: String) {

    companion object {
        private const val TAG = "GoogleNativeAd"
        private const val MAX_LOAD_NUM = 15
    }

    abstract fun setNativeAd(ad: NativeAd?)

    private val mBuilder: AdLoader.Builder = AdLoader.Builder(context, adId)
    private val mAdLoader: AdLoader
    private var mNativeAd: NativeAd? = null
    private var isAdLoaded = false
    private var numberOfLoad = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            timerHandler.removeCallbacksAndMessages(null)
            loadOneAd()
            // 3 minutes later
            timerHandler.postDelayed(this, 300000)
        }
    }

    init {
        mBuilder.forNativeAd { nativeAd ->
            Log.d(TAG, "forNativeAd().nativeAd = $nativeAd")
            mNativeAd?.destroy()
            mNativeAd = nativeAd
            setNativeAd(mNativeAd)
        }
        mAdLoader = mBuilder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d(TAG, "onAdFailedToLoad().Failed to load")
                Log.d(TAG,"onAdFailedToLoad().mNumberOfLoad = $numberOfLoad")
                isAdLoaded = false
                timerHandler.removeCallbacksAndMessages(null)
                if (numberOfLoad < MAX_LOAD_NUM) {
                    Log.d(TAG,"LonAdFailedToLoad().loadOneAd()")
                    loadOneAd()
                    numberOfLoad++
                } else {
                    numberOfLoad = 0 // set back to zero
                    Log.d(TAG,"onAdFailedToLoad().Failed " +
                            "more than$MAX_LOAD_NUM, so stopped loading.")
                    // 5 minutes later
                    timerHandler.postDelayed(timerRunnable, 500000)
                }
            }
            override fun onAdLoaded() {
                Log.d(TAG, "onAdLoaded.Succeeded.numberOfLoad = $numberOfLoad")
                numberOfLoad = 0    // set to 0
                isAdLoaded = true
            }
        }).build()

        // loadOneAd()
        timerHandler.post(timerRunnable)
    }

    private fun loadOneAd() {
        Log.d(TAG, "loadOneAd().numberOfLoad = $numberOfLoad")
        mAdLoader.loadAd(AdRequest.Builder().build())
    }
}