package com.smile.smilelibraries.privacy_policy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.window.OnBackInvokedDispatcher;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Implement your custom back logic here
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    this::returnToCallingIntent
            );
        } else {
            // For older Android versions, use OnBackPressedDispatcher
            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Implement your custom back logic here for older versions
                    returnToCallingIntent();
                }
            });
        }
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

    private void returnToCallingIntent() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
