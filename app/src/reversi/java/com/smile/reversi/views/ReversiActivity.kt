package com.smile.reversi.views

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.smile.colorballs_main.R
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.CbComposable
import com.smile.colorballs_main.views.CbRmBaseView
import com.smile.reversi.interfaces.ReversiPresentView
import com.smile.reversi.presenters.ReversiPresenter
import com.smile.reversi.viewmodels.ReversiViewModel

class ReversiActivity: CbRmBaseView(), ReversiPresentView {

    companion object {
        private const val TAG = "ReversiActivity"
    }

    private lateinit var viewModel: ReversiViewModel
    private lateinit var mPresenter: ReversiPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate.savedInstanceState = $savedInstanceState")
        mPresenter = ReversiPresenter(this)
        viewModel = ReversiViewModel(mPresenter)
        super.onCreate(savedInstanceState)
    }

    // implement ReversiPresentView
    override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)

    // implement GameOptions
    override fun setWhichGame() {
        LogUtil.i(TAG, "setWhichGame")
        viewModel.setWhichGame(WhichGame.REMOVE_BALLS)
    }

    @Composable
    override fun CreateNewGameDialog() {
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
                this@ReversiActivity,
                buttonListener, "", dialogText
            )
        }
    }

    override fun getCurrentPresenter(): ReversiPresenter {
        return mPresenter
    }

    override fun getCurrentViewModel(): ReversiViewModel {
        return viewModel
    }

    override fun ifInterstitialWhenSaveScore() {
        // no-op for now
    }

    override fun ifInterstitialWhenNewGame() {
        viewModel.initGame(bundle = null)
    }

    override fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int) {
        if (newGameLevel != originalLevel) {
            viewModel.isCreatingNewGame()
        }
    }

    override fun setHasNextForView(hasNext: Boolean) {
        viewModel.setHasNext(hasNext)
    }
}