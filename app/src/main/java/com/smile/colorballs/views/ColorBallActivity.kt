package com.smile.colorballs.views

import android.util.Log
import com.smile.colorballs.constants.WhichGame
import com.smile.colorballs.interfaces.GameOptions

class ColorBallActivity : CBallBaseActivity(), GameOptions {

    private val mTAG : String = "ColorBallActivity"
    init {
        setTag(mTAG)
    }

    override fun setWhichGame() {
        Log.d(mTAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.NO_BARRIER)
    }
}