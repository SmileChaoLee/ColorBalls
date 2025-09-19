package com.smile.colorballs.views

import android.util.Log

class BarrierCBallActivity : ColorBallActivity() {
    private val mTAG : String = "BarrierCBallActivity"
    init {
        Log.d(mTAG, "")
        setTag(mTAG)
        whichGame = 1  // Random distribution
    }
}