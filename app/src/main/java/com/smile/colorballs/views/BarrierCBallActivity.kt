package com.smile.colorballs.views

import android.util.Log
import com.smile.colorballs.constants.WhichGame
import com.smile.colorballs.interfaces.GameOptions

class BarrierCBallActivity : CBallBaseActivity(), GameOptions {

    private val mTAG : String = "BarrierCBActivity"
    init {
        setTag(mTAG)
    }

    override fun setWhichGame() {
        Log.d(mTAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.HAS_BARRIER)
    }
}