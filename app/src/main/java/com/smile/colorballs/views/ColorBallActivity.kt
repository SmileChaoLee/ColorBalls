package com.smile.colorballs.views

import android.util.Log
import com.smile.colorballs.constants.WhichGame
import com.smile.colorballs.interfaces.GameOptions

class ColorBallActivity : CBallView() {

    override fun setWhichGame() {
        Log.d(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.NO_BARRIER)
    }

    companion object {
        private const val TAG : String = "ColorBallActivity"
    }
}