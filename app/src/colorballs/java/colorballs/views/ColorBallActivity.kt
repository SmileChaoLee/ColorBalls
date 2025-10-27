package colorballs.views

import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil

class ColorBallActivity : CBallView() {

    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.NO_BARRIER)
    }

    companion object {
        private const val TAG : String = "ColorBallActivity"
    }
}