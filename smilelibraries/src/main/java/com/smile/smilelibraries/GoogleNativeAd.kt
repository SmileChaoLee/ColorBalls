package com.smile.smilelibraries

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
        private const val TWO_MINUTES = 120000L // 120 seconds
        private const val ONE_MINUTE = 60000L    // 60 seconds
    }

    abstract fun setNativeAd(ad: NativeAd?)

    private val mBuilder: AdLoader.Builder = AdLoader.Builder(context, adId)
    private val mAdLoader: AdLoader
    private var mNativeAd: NativeAd? = null
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            timerHandler.removeCallbacksAndMessages(null)
            loadOneAd()
            timerHandler.postDelayed(this, TWO_MINUTES)
        }
    }
    private var isAdLoaded = false
    private var numberOfLoad = 0

    init {
        mBuilder.forNativeAd { nativeAd ->
            Log.d(TAG, "forNativeAd().adId = $adId")
            Log.d(TAG, "forNativeAd().nativeAd = $nativeAd")
            mNativeAd?.destroy()
            mNativeAd = nativeAd
            setNativeAd(mNativeAd)
        }
        mAdLoader = mBuilder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.d(TAG,"onAdFailedToLoad.adId = $adId")
                Log.d(TAG,"onAdFailedToLoad.mNumberOfLoad = $numberOfLoad")
                isAdLoaded = false
                timerHandler.removeCallbacksAndMessages(null)
                if (numberOfLoad < MAX_LOAD_NUM) {
                    numberOfLoad++
                    timerHandler.postDelayed(timerRunnable, ONE_MINUTE)
                } else {
                    Log.d(TAG,"onAdFailedToLoad.adId = $adId")
                    Log.d(TAG,"onAdFailedToLoad.more than $MAX_LOAD_NUM")
                    numberOfLoad = 0 // set back to zero
                    timerHandler.postDelayed(timerRunnable, TWO_MINUTES)
                }
            }
            override fun onAdLoaded() {
                Log.d(TAG,"onAdLoaded.adId = $adId")
                Log.d(TAG, "onAdLoaded.numberOfLoad = $numberOfLoad")
                isAdLoaded = true
                numberOfLoad = 0    // set to 0
            }
        }).build()

        timerHandler.post(timerRunnable)
        Log.d(TAG, "GoogleNativeAd for $adId created")
    }

    private fun loadOneAd() {
        Log.d(TAG, "loadOneAd().adId = $adId")
        Log.d(TAG, "loadOneAd().numberOfLoad = $numberOfLoad")
        mAdLoader.loadAd(AdRequest.Builder().build())
    }
}