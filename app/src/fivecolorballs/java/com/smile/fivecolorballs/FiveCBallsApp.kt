package com.smile.fivecolorballs

import androidx.multidex.MultiDexApplication

class FiveCBallsApp : MultiDexApplication() {

    companion object {
        const val ADMOB_BANNER_ID = "ca-app-pub-8354869049759576/7162646323"
        const val ADMOB_BANNER_ID2 = "ca-app-pub-8354869049759576/5784271650"
        const val ADMOB_NATIVE_ID = "ca-app-pub-8354869049759576/8621863614"
    }

    override fun onCreate() {
        super.onCreate()
    }
}