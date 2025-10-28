package colorballs.views

import android.content.Intent
import com.smile.colorballs_main.R
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.BaseCBallActivity

class MainCBallActivity : BaseCBallActivity() {

    override fun isBallsRemover(): Boolean {
        return false
    }

    override fun startColorBallActivity() {
        Intent(
            this@MainCBallActivity,
            ColorBallActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            cBallLauncher.launch(it)
        }
    }

    override fun startBarrierCBallActivity() {
        Intent(
            this@MainCBallActivity,
            BarrierCBallActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            barrierCBLauncher.launch(it)
        }
    }

    private val mTAG : String = "MainCBallActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}