package com.smile.smilelibraries.privacy_policy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.smile.smilelibraries.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy_policy);

        WebView privacyPolicyWebView = findViewById(R.id.privacyPolicyWebView);
        privacyPolicyWebView.getSettings().setJavaScriptEnabled(true);
        privacyPolicyWebView.loadUrl("https://smilechaolee.github.io/PrivacyPolicy");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        returnToCallingIntent();
    }

    private void returnToCallingIntent() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
