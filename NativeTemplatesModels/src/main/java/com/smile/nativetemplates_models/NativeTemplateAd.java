package com.smile.nativetemplates_models;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAd;

public class NativeTemplateAd {
    private static final String TAG = new String("NativeTemplateAd");

    private final Context mContext;
    private final String mAdUnitId;
    private final int mMaxNumberOfLoad = 15;
    private final TemplateView mNativeAdTemplateView;
    private final AdLoader mNativeAdLoader;
    private NativeAd mNativeAd;
    private boolean isNativeAdLoaded;
    private int mNumberOfLoad;

    public NativeTemplateAd(Context context, String nativeAdID, TemplateView templateView) {
        mContext = context;
        mAdUnitId = nativeAdID;
        mNativeAdTemplateView = templateView;
        mNativeAd = null;
        mNumberOfLoad = 0;
        AdLoader.Builder builder = null;
        boolean isInitialized = true;
        try {
            if (mAdUnitId != null) {
                if (!mAdUnitId.isEmpty()) {
                    builder = new AdLoader.Builder(mContext, mAdUnitId);
                    builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        // OnNativeAdLoadedListener implementation.
                        @Override
                        public void onNativeAdLoaded(NativeAd nativeAd) {
                            // You must call destroy on old ads when you are done with them,
                            // otherwise you will have a memory leak.
                            if (mNativeAd != null) {
                                mNativeAd.destroy();
                            }
                            mNativeAd = nativeAd;
                            isNativeAdLoaded = true;
                            mNumberOfLoad = 0;

                            // start to show ad
                            mNativeAdTemplateView.setVisibility(View.VISIBLE);
                            ColorDrawable background = new ColorDrawable(Color.WHITE);
                            NativeTemplateStyle styles = new
                                    NativeTemplateStyle.Builder().withMainBackgroundColor(background).build();

                            mNativeAdTemplateView.setStyles(styles);
                            mNativeAdTemplateView.setNativeAd(nativeAd);
                            //

                            Log.d(TAG, "onNativeAdLoaded() --> Succeeded to load NativeAd.");
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "Failed to initialize NativeAd.");
            ex.printStackTrace();
            isInitialized = false;
        }

        if (isInitialized) {
            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();

            builder.withNativeAdOptions(adOptions);

            mNativeAdLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.d(TAG, "onAdFailedToLoad() --> Failed to load NativeAd.");
                    Log.d(TAG, "onAdFailedToLoad() --> mMaxNumberOfLoad = " + mMaxNumberOfLoad + ", mNumberOfLoad = " + mNumberOfLoad);
                    isNativeAdLoaded = false;
                    if (mNumberOfLoad<mMaxNumberOfLoad) {
                        loadOneAd();
                        Log.d(TAG, "onAdFailedToLoad() --> Load again --> mNumberOfLoad = " + mNumberOfLoad);
                    } else {
                        mNumberOfLoad = 0;   // set back to zero
                        Log.d(TAG, "onAdFailedToLoad() --> Failed to load NativeAd more than" + mMaxNumberOfLoad + ".\n So stopped loading this time.");
                    }
                }

                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "onAdLoaded() --> Succeeded to load NativeAd.");
                    Log.d(TAG, "onAdLoaded() --> mMaxNumberOfLoad = " + mMaxNumberOfLoad + ", mNumberOfLoad = " + mNumberOfLoad);
                }
            }).build();
        } else {
            mNativeAdLoader = null;
        }
    }

    public void loadOneAd() {
        Log.d(TAG, "loadOneAd() is called.");
        if (mNativeAdLoader != null) {
            mNativeAdLoader.loadAd(new AdRequest.Builder().build());
            Log.d(TAG, "loadOneAd() --> mNumberOfLoad = " + mNumberOfLoad);
            mNumberOfLoad++;
        }
    }

    public AdLoader getNativeAdLoader() {
        return mNativeAdLoader;
    }

    public NativeAd getNativeAd() {
        return mNativeAd;
    }

    public boolean isNativeAdLoaded() {
        return isNativeAdLoaded;
    }

    public void releaseNativeAd() {
        if (mNativeAd != null) {
            mNativeAd.destroy();
        }
    }
}
