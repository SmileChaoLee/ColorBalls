package ballsremover.views

import android.content.Intent
import com.smile.colorballs_main.R
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.BaseCBallActivity

class MainBallRmActivity : BaseCBallActivity() {

    override fun isBallsRemover(): Boolean {
        return true
    }

    override fun startBallsRemoverActivity() {
        Intent(
            this@MainBallRmActivity,
            BallsRemoverActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            ballsRemoverLauncher.launch(it)
        }
    }

    private val mTAG : String = "MainBallRmActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}