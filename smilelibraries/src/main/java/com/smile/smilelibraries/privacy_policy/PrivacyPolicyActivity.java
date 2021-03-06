package com.smile.smilelibraries.privacy_policy;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.smile.smilelibraries.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private final String privacyPolicyUrl = "http://ec2-13-59-195-3.us-east-2.compute.amazonaws.com/PrivacyPolicy";
    private WebView privacyPolicyWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy_policy);

        privacyPolicyWebView = findViewById(R.id.privacyPolicyWebView);
        privacyPolicyWebView.getSettings().setJavaScriptEnabled(true);
        privacyPolicyWebView.loadUrl(privacyPolicyUrl);
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
