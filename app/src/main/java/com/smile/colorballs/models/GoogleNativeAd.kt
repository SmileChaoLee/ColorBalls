package com.smile.colorballs.models

import android.content.Context
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
    }

    abstract fun setNativeAd(ad: NativeAd?)

    private val mBuilder: AdLoader.Builder = AdLoader.Builder(context, adId)
    private val mAdLoader: AdLoader
    private var mNativeAd: NativeAd? = null
    private var isAdLoaded = false
    private var numberOfLoad = 0

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
                if (numberOfLoad < MAX_LOAD_NUM) {
                    Log.d(TAG,"LonAdFailedToLoad().loadOneAd()")
                    loadOneAd()
                    numberOfLoad++
                } else {
                    numberOfLoad = 0 // set back to zero
                    Log.d(TAG,"onAdFailedToLoad().Failed " +
                            "more than$MAX_LOAD_NUM, so stopped loading.")
                }
            }
            override fun onAdLoaded() {
                Log.d(TAG, "onAdLoaded.Succeeded.numberOfLoad = $numberOfLoad")
                numberOfLoad = 0    // set to 0
                isAdLoaded = true
            }
        }).build()

        loadOneAd()
    }

    private fun loadOneAd() {
        Log.d(TAG, "loadOneAd().numberOfLoad = $numberOfLoad")
        mAdLoader.loadAd(AdRequest.Builder().build())
    }
}