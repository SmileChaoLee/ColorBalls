package colorballs.views

import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil

class BarrierCBallActivity : CBallView() {

    companion object {
        private const val TAG : String = "BarrierCBActivity"
    }

    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.HAS_BARRIER)
    }
}