package com.smile.colorballs.ballsremover.views

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.smile.colorballs.R
import com.smile.colorballs.ballsremover.constants.BallsRmConstants
import com.smile.colorballs.ballsremover.interfaces.BallsRmPresentView
import com.smile.colorballs.ballsremover.presenters.BallsRmPresenter
import com.smile.colorballs.ballsremover.viewmodels.BallsRmViewModel
import com.smile.colorballs.tools.LogUtil
import com.smile.colorballs.views.CbComposable
import com.smile.colorballs.views.MyView

abstract class BallsRmView: MyView(), BallsRmPresentView {

    protected lateinit var viewModel: BallsRmViewModel
    private lateinit var mPresenter: BallsRmPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate.savedInstanceState = $savedInstanceState")
        // Must be before super.onCreate(savedInstanceState)
        mPresenter = BallsRmPresenter(this)
        viewModel = BallsRmViewModel(mPresenter)

        super.onCreate(savedInstanceState)
    }

    // implement BallsRmPresentView
    override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)
    // end of implementing BallsRmPresentView

    // implement abstract fun of MyView
    @Composable
    override fun CreateNewGameDialog() {
        LogUtil.i(TAG, "CreateNewGameDialog")
        val dialogText = viewModel.getCreateNewGameText()
        if (dialogText.isNotEmpty()) {
            viewModel.setShowingCreateGameDialog(true)
            val buttonListener = object: CbComposable.ButtonClickListener {
                override fun buttonOkClick() {
                    viewModel.setCreateNewGameText("")
                    viewModel.setShowingCreateGameDialog(false)
                    quitOrNewGame()
                }
                override fun buttonCancelClick() {
                    viewModel.setCreateNewGameText("")
                    viewModel.setShowingCreateGameDialog(false)
                }
            }
            CbComposable.DialogWithText(
                this@BallsRmView,
                buttonListener, "", dialogText
            )
        }
    }

    override fun getBasePresenter(): BallsRmPresenter {
        return mPresenter
    }

    override fun getBaseViewModel(): BallsRmViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        LogUtil.i(TAG, "ifShowInterstitialAd")
        if (viewModel.timesPlayed >= BallsRmConstants.SHOW_ADS_AFTER_TIMES) {
            LogUtil.d(TAG, "ifShowInterstitialAd.showInterstitialAd")
            showInterstitialAd()
            viewModel.timesPlayed = 0
        }
    }

    override fun ifInterstitialWhenNewGame() {
        LogUtil.i(TAG, "ifInterstitialWhenNewGame")
        // viewModel.initGame(null) // will make the setting back to normal
        viewModel.initGame(bundle = null)
    }

    override fun ifCreatingNewGame(newEasyLevel: Boolean, originalLevel: Boolean) {
        if (newEasyLevel != originalLevel) {
            // game levels are different, create a new game?
            viewModel.isCreatingNewGame()
        }
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext)
    }
    // end of implementing abstract fun of MyView

    companion object {
        private const val TAG = "BallsRmView"
    }
}