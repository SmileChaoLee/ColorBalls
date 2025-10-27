package colorballs.views

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.smile.colorballs_main.R
import colorballs.interfaces.CBallPresentView
import colorballs.presenters.CBallPresenter
import com.smile.colorballs_main.tools.LogUtil
import colorballs.viewmodels.CBallViewModel
import com.smile.colorballs_main.views.MyView
import com.smile.smilelibraries.interfaces.DismissFunction

abstract class CBallView: MyView(), CBallPresentView {

    companion object {
        private const val TAG = "CBallView"
    }
    protected lateinit var viewModel: CBallViewModel
    private lateinit var mPresenter: CBallPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate")
        // Must be before super.onCreate(savedInstanceState)
        mPresenter = CBallPresenter(this)
        viewModel = CBallViewModel(mPresenter)

        super.onCreate(savedInstanceState)
    }

    // implementing PresentViewCompose
    @Composable
    override fun CreateNewGameDialog() {
        // do nothing
    }

    override fun getGameOverStr() = getString(R.string.gameOverStr)
    // end of implementing

    // implement abstract fun of MyView
    override fun getBasePresenter(): CBallPresenter {
        return mPresenter
    }

    override fun getBaseViewModel(): CBallViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        // do nothing
    }

    override fun ifCreatingNewGame(newEasyLevel: Boolean, originalLevel: Boolean) {
        // do nothing
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext, true)
    }
    // end of implementing abstract fun of MyView

    override fun ifInterstitialWhenNewGame() {
        LogUtil.i(TAG, "ifInterstitialWhenNewGame")
        interstitialAd?.apply {
            ShowAdThread(object : DismissFunction {
                override fun backgroundWork() {
                    LogUtil.d(TAG, "backgroundWork")
                }
                override fun executeDismiss() {
                    LogUtil.d(TAG, "executeDismiss")
                    viewModel.initGame(null)
                }
                override fun afterFinished(isAdShown: Boolean) {
                    LogUtil.d(TAG, "afterFinished.isAdShown= $isAdShown")
                    if (!isAdShown) viewModel.initGame(null)
                }
            }).startShowAd(0)
        }
    }
    // end of implementing abstract fun of MyView
}