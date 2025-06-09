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

abstract class GoogleNativeAd(
    private val context: Context,
    private val adId: String) {

    companion object {
        private const val TAG = "GoogleNativeAd"
        private const val MAX_LOAD_NUM = 15
        private const val FIVE_MINUTES = 300000L // 300 seconds
        private const val ONE_MINUTE = 60000L    // 60 seconds
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
            // 5 minutes later
            timerHandler.postDelayed(this, FIVE_MINUTES)
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
                Log.d(TAG,"onAdFailedToLoad().mNumberOfLoad = $numberOfLoad")
                isAdLoaded = false
                timerHandler.removeCallbacksAndMessages(null)
                if (numberOfLoad < MAX_LOAD_NUM) {
                    Log.d(TAG,"LonAdFailedToLoad().loadOneAd()")
                    numberOfLoad++
                    // 60 seconds later
                    timerHandler.postDelayed(timerRunnable, ONE_MINUTE)
                } else {
                    numberOfLoad = 0 // set back to zero
                    Log.d(TAG,"onAdFailedToLoad().Failed " +
                            "more than $MAX_LOAD_NUM")
                    // 5 minutes later
                    timerHandler.postDelayed(timerRunnable, FIVE_MINUTES)
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