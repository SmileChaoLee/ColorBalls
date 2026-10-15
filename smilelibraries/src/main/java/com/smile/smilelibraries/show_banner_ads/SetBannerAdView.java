package com.smile.smilelibraries.show_banner_ads;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;

public class SetBannerAdView {
    private final static String TAG = "SetBannerAdView";
    private final Context mContext;
    private final LinearLayout mCompanyInfoLayout;
    private final LinearLayout mBannerLinearLayout;
    private final String mGoogleAdMobBannerID;
    private final boolean isAdMobAvailable;
    private final int mBannerDpWidth;
    private com.google.android.gms.ads.AdView adMobBannerAdView;
    private int numberOfLoadingAdMobBannerAd = 0;

    public SetBannerAdView(Context context, LinearLayout companyInfoLayout
            , LinearLayout bannerLinearLayout
            , String googleAdMobBannerID) {
        this(context, companyInfoLayout, bannerLinearLayout, googleAdMobBannerID, 0);
    }

    public SetBannerAdView(Context context,
                           LinearLayout companyInfoLayout,
                           LinearLayout bannerLinearLayout,
                           String googleAdMobBannerID,
                           int bannerDpWidth) {
        mContext = context;
        mCompanyInfoLayout = companyInfoLayout;
        mBannerLinearLayout = bannerLinearLayout;
        mGoogleAdMobBannerID = googleAdMobBannerID;
        mBannerDpWidth = bannerDpWidth;
        isAdMobAvailable = (googleAdMobBannerID!=null && !googleAdMobBannerID.isEmpty());
        if (bannerLinearLayout != null) bannerLinearLayout.removeAllViews();
    }

    public void showBannerAdView() {
        if (!isAdMobAvailable) {
            // no banner ads so show company information
            if (mBannerLinearLayout != null) {
                mBannerLinearLayout.setVisibility(View.GONE);
            }
            if (mCompanyInfoLayout != null) {
                mCompanyInfoLayout.setVisibility(View.VISIBLE);
            }
            return;
        }
        if (mCompanyInfoLayout != null) {
            mCompanyInfoLayout.setVisibility(View.GONE);
        }
        setAdMobBannerAdView();
    }

    public void resume() {
        if (adMobBannerAdView != null) {
            adMobBannerAdView.resume();
        }
    }

    public void pause() {
        if (adMobBannerAdView != null) {
            adMobBannerAdView.pause();
        }
    }

    public void destroy() {
        if (adMobBannerAdView != null) {
            adMobBannerAdView.destroy();
            adMobBannerAdView = null;
        }
    }

    private void setAdMobBannerAdView() {
        Log.d(TAG, "setAdMobBannerAdView() is called.");
        Log.d(TAG, "setAdMobBannerAdView.Starting AdMob Banner Ad.");
        numberOfLoadingAdMobBannerAd = 0;
        adMobBannerAdView = new com.google.android.gms.ads.AdView(mContext);
        if (mBannerDpWidth <= 0) {
            adMobBannerAdView.setAdSize(AdSize.BANNER);
        } else {
            // adaptive banner
            AdSize adMobAdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize (mContext, mBannerDpWidth);
            adMobBannerAdView.setAdSize(adMobAdSize);
        }
        adMobBannerAdView.setVisibility(View.VISIBLE);
        adMobBannerAdView.setAdUnitId(mGoogleAdMobBannerID);
        mBannerLinearLayout.addView(adMobBannerAdView);
        AdListener adMobBannerListener = new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "Could not load Google AdMob Banner ad.numberOfLoadingAdMobBannerAd = " +
                        numberOfLoadingAdMobBannerAd);
                numberOfLoadingAdMobBannerAd++;
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "Succeeded to load AdMob Banner ad.googleAdMobBannerID = " + mGoogleAdMobBannerID);
                numberOfLoadingAdMobBannerAd = 0;
            }
        };

        adMobBannerAdView.setAdListener(adMobBannerListener);
        AdRequest adMobBannerAdRequest = new AdRequest.Builder().build();
        adMobBannerAdView.loadAd(adMobBannerAdRequest);
    }
}
