package com.smile.smilepublicclasseslibrary.privacy_policy;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.smile.smilepublicclasseslibrary.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private WebView privacyPolicyWebView;
    private String privacyPolicyUrl;
    private Intent callingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privacyPolicyUrl = "";
        callingIntent = getIntent();
        privacyPolicyUrl = callingIntent.getStringExtra("PrivacyPolicyWebsite");
        if ( (privacyPolicyUrl == null) || (privacyPolicyUrl.isEmpty()) ) {
            returnToCallingIntent();
        }

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
