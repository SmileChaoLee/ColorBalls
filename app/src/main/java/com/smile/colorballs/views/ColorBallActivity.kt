package com.smile.colorballs.views

import com.smile.colorballs.constants.WhichGame
import com.smile.colorballs.tools.LogUtil

class ColorBallActivity : CBallView() {

    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.NO_BARRIER)
    }

    companion object {
        private const val TAG : String = "ColorBallActivity"
    }
}