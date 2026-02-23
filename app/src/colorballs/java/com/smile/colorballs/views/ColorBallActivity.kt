package com.smile.colorballs.views

import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil

class ColorBallActivity : CBallView() {

    companion object {
        private const val TAG : String = "ColorBallActivity"
    }

    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.NO_BARRIER)
    }
}