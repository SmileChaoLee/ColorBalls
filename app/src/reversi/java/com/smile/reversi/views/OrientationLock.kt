package com.smile.reversi.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo

private const val LARGE_SCREEN_DP = 600

/** Portrait only on phones; tablets and foldables (smallest width >= 600dp) may rotate. */
@SuppressLint("SourceLockedOrientationActivity")
fun Activity.lockPortraitOnPhones() {
    if (resources.configuration.smallestScreenWidthDp < LARGE_SCREEN_DP) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
