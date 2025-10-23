package com.smile.colorballs.views

import com.smile.colorballs.constants.WhichGame
import com.smile.colorballs.tools.LogUtil

class BarrierCBallActivity : CBallView() {

    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.HAS_BARRIER)
    }

    companion object {
        private const val TAG : String = "BarrierCBActivity"
    }
}