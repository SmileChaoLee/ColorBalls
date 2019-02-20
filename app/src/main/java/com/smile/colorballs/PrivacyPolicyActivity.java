package com.smile.colorballs;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.smile.smilepublicclasseslibrary.utilities.ScreenUtil;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String privacyPolicyUrl = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/PrivacyPolicy";
    private WebView privacyPolicyWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ScreenUtil.isTablet(this)) {
            // Table then change orientation to Landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            // phone then change orientation to Portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_privacy_policy);

        privacyPolicyWebView = findViewById(R.id.privacyPolicyWebView);
        privacyPolicyWebView.getSettings().setJavaScriptEnabled(true);
        privacyPolicyWebView.loadUrl(privacyPolicyUrl);
    }

    @Override
    public void onPause() {
        privacyPolicyWebView.onPause();
        privacyPolicyWebView.pauseTimers();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        privacyPolicyWebView.resumeTimers();
        privacyPolicyWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        privacyPolicyWebView.clearHistory();
        // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
        // Probably not a great idea to pass true if you have other WebViews still alive.
        privacyPolicyWebView.clearCache(true);
        privacyPolicyWebView.loadUrl("about:blank");

        // privacyPolicyWebView.onPause();
        privacyPolicyWebView.removeAllViews();
        privacyPolicyWebView.destroyDrawingCache();

        privacyPolicyWebView.destroy();
        privacyPolicyWebView = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
