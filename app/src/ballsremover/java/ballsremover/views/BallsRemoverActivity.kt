package ballsremover.views

import android.os.Bundle
import android.view.MotionEvent
import com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil
import com.smile.smilelibraries.utilities.UmpUtil


class BallsRemoverActivity : BallsRmView() {

    private var touchDisabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate")
        super.onCreate(savedInstanceState)
        // disabling the touch events
        touchDisabled = true
        val deviceHashedId = "8F6C5B0830E624E8D8BFFB5853B4EDDD" // for debug test
        // val deviceHashedId = "" // for release
        UmpUtil.initConsentInformation(this@BallsRemoverActivity,
            DEBUG_GEOGRAPHY_EEA,deviceHashedId,
            object : UmpUtil.UmpInterface {
                override fun callback() {
                    LogUtil.d(TAG, "onCreate.initConsentInformation.finished")
                    // enabling receiving touch events
                    touchDisabled = false
                }
            })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (touchDisabled) {
            // Consume the touch event, effectively disabling touch
            return true
        }
        // Allow touch events to proceed
        return super.dispatchTouchEvent(ev)
    }

    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.REMOVE_BALLS)
    }

    companion object {
        private const val TAG = "BallsRemoActivity"
    }
}
