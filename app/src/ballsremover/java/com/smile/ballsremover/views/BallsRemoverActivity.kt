package com.smile.ballsremover.views

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.smile.colorballs_main.R
import com.smile.ballsremover.constants.BallsRmConstants
import com.smile.ballsremover.interfaces.BallsRmPresentView
import com.smile.ballsremover.presenters.BallsRmPresenter
import com.smile.ballsremover.viewmodels.BallsRmViewModel
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.CbComposable
import com.smile.colorballs_main.views.CbRmBaseView

class BallsRemoverActivity: CbRmBaseView(), BallsRmPresentView {

    companion object {
        private const val TAG = "BallsRemActivity"
    }

    private lateinit var viewModel: BallsRmViewModel
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

    // implement interface, GameOptions
    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.REMOVE_BALLS)
    }
    // end of implementing interface, GameOptions

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
                this@BallsRemoverActivity,
                buttonListener, "", dialogText
            )
        }
    }

    override fun getCurrentPresenter(): BallsRmPresenter {
        return mPresenter
    }

    override fun getCurrentViewModel(): BallsRmViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        LogUtil.i(TAG, "ifShowInterstitialAd")
        if (viewModel.timesPlayed >= BallsRmConstants.SHOW_ADS_AFTER_TIMES) {
            LogUtil.d(TAG, "ifShowInterstitialAd.showInterstitialAd")
            // showInterstitialAd()
            viewModel.timesPlayed = 0
        }
    }

    override fun ifInterstitialWhenNewGame() {
        LogUtil.i(TAG, "ifInterstitialWhenNewGame")
        viewModel.initGame(bundle = null)
    }

    override fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int) {
        if (newGameLevel != originalLevel) {
            // game levels are different, create a new game?
            viewModel.isCreatingNewGame()
        }
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext)
    }
    // end of implementing abstract fun of MyView
}