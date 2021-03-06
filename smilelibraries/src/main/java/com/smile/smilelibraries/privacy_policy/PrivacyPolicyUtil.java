package com.smile.smilelibraries.privacy_policy;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

public class PrivacyPolicyUtil {

    private PrivacyPolicyUtil(){};

    public static void startPrivacyPolicyActivity(Activity mActivity, int requestCode) {
        // Intent myIntent = new Intent(mActivity, PrivacyPolicyActivity.class);
        Intent myIntent = new Intent();
        // Needed to set component to remove explicit activity entry in application's manifest
        final ComponentName component = new ComponentName(mActivity, PrivacyPolicyActivity.class);
        myIntent.setComponent(component);
        mActivity.startActivityForResult(myIntent, requestCode);
    }
}
