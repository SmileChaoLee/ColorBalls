package com.smile.nativetemplates_models;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

// import com.smile.nativetemplates_models.NativeTemplateAd;

public class GoogleAdMobNativeTemplate {
    private static final String TAG = new String(".GoogleAdMobNativeTemplate");
    private final Context mContext;
    private final FrameLayout mNativeAdsFrameLayout;
    private final String mNativeAdvancedId0;
    private final NativeTemplateAd mNativeTemplateAd;
    private final com.google.android.ads.nativetemplates.TemplateView mNativeAdTemplateView;

    private final Handler showNativeAdTimerHandler = new Handler(Looper.getMainLooper());
    private final Runnable showNativeAdTimerRunnable = new Runnable() {
        @Override
        public void run() {
            showNativeAdTimerHandler.removeCallbacksAndMessages(null);
            if (mNativeTemplateAd != null) {
                Log.d(TAG, "showNativeAdTimerRunnable() --> Loading AdMob native ad.");
                mNativeTemplateAd.loadOneAd();
                showNativeAdTimerHandler.postDelayed(this, 300000); // 5 minutes
            }
        }
    };

    public GoogleAdMobNativeTemplate(Context context, FrameLayout nativeAdsFrameLayout, String nativeAdvancedId0
            ,com.google.android.ads.nativetemplates.TemplateView nativeAdTemplateView) {
        this.mContext = context;
        this.mNativeAdsFrameLayout = nativeAdsFrameLayout;
        this.mNativeAdvancedId0 = nativeAdvancedId0;
        this.mNativeAdTemplateView = nativeAdTemplateView;
        boolean hasNativeId = false;
        if (nativeAdvancedId0 != null) {
            if (!nativeAdvancedId0.isEmpty()) {
                hasNativeId = true;
            }
        }
        if (hasNativeId) {
            this.mNativeTemplateAd = new NativeTemplateAd(context, nativeAdvancedId0
                    , nativeAdTemplateView);
        } else {
            this.mNativeTemplateAd = null;
            hideNativeAd();
        }
    }

    public void showNativeAd() {
        Log.d(TAG, "showNativeAd() is called.");
        if ( (mNativeAdsFrameLayout != null) && (mNativeTemplateAd != null) ) {
            Log.d(TAG, "showNativeAd() --> nativeAdsFrameLayout.setVisibility(View.VISIBLE)");
            mNativeAdsFrameLayout.setVisibility(View.VISIBLE);
            if (showNativeAdTimerHandler != null) {
                showNativeAdTimerHandler.post(showNativeAdTimerRunnable);
            }
        }
    }

    public void hideNativeAd() {
        Log.d(TAG, "hideNativeAd() is called.");
        if (mNativeAdsFrameLayout != null) {
            mNativeAdsFrameLayout.setVisibility(View.GONE);
        }
        if (showNativeAdTimerHandler != null) {
            showNativeAdTimerHandler.removeCallbacksAndMessages(null);
        }
    }

    public void release() {
        if (mNativeTemplateAd != null) {
            mNativeTemplateAd.releaseNativeAd();
        }
        if (showNativeAdTimerHandler != null) {
            showNativeAdTimerHandler.removeCallbacksAndMessages(null);
        }
    }
}
