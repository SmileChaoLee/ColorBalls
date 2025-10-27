package ballsremover.views

import android.os.Bundle
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil

class BallsRemoverActivity : BallsRmView() {

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate")
        super.onCreate(savedInstanceState)
    }

    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.REMOVE_BALLS)
    }

    companion object {
        private const val TAG = "BallsRemoActivity"
    }
}
