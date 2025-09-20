package com.smile.colorballs.views

import android.util.Log
import com.smile.colorballs.constants.WhichGame

class BarrierCBallActivity : ColorBallActivity() {
    private val mTAG : String = "BarrierCBallActivity"
    init {
        Log.d(mTAG, "")
        setTag(mTAG)
        whichGame = WhichGame.HAS_BARRIER
    }
}