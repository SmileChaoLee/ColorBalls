package com.smile.colorballs.views

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.smile.colorballs_main.R
import com.smile.colorballs.interfaces.CBallPresentView
import com.smile.colorballs.presenters.CBallPresenter
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs.viewmodels.CBallViewModel
import com.smile.colorballs_main.views.CbRmBaseView

abstract class CBallView: CbRmBaseView(), CBallPresentView {

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

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        viewModel.release()
    }

    // implementing PresentViewCompose
    @Composable
    override fun CreateNewGameDialog() {
        // do nothing
        LogUtil.i(TAG, "CreateNewGameDialog")
    }

    override fun getGameOverStr() = getString(R.string.gameOverStr)
    // end of implementing

    // implement abstract fun of MyView
    override fun getCurrentPresenter(): CBallPresenter {
        return mPresenter
    }

    override fun getCurrentViewModel(): CBallViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        LogUtil.i(TAG, "ifShowInterstitialAd")
        // do nothing
    }

    override fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int) {
        // do nothing
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext, true)
    }
    // end of implementing abstract fun of MyView

    override fun ifInterstitialWhenNewGame() {
        LogUtil.i(TAG, "ifInterstitialWhenNewGame")
        // showInterstitialAd()
        viewModel.initGame(bundle = null)
        // do not use the following, it does not work sometimes
        /*
        interstitialAd?.apply {
            ShowAdThread(object : DismissFunction {
                override fun backgroundWork() {
                    LogUtil.d(TAG, "backgroundWork")
                }
                override fun executeDismiss() {
                    // dismiss does not work sometimes
                    LogUtil.d(TAG, "executeDismiss")
                    viewModel.initGame(null)
                }
                override fun afterFinished(isAdShown: Boolean) {
                    LogUtil.d(TAG, "afterFinished.isAdShown= $isAdShown")
                    if (!isAdShown) viewModel.initGame(null)
                }
            }).startShowAd(0)
        }
        */
    }
    // end of implementing abstract fun of MyView
}