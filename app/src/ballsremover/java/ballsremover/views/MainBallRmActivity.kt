package ballsremover.views

import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.BaseCBallActivity

class MainBallRmActivity : BaseCBallActivity() {

    override fun isBallsRemover(): Boolean {
        return true
    }

    private val mTAG : String = "MainBallRmActivity"
    init {
        LogUtil.d(mTAG, "")
        setTag(mTAG)
    }
}