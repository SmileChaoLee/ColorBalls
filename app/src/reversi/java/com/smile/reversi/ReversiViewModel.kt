package com.smile.reversi

import android.os.Bundle
import android.graphics.Point
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.viewmodel.BaseViewModel
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.tools.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReversiViewModel(private val rPresenter: ReversiPresenter)
    : BaseViewModel(rPresenter) {

    companion object {
        private const val TAG = "ReversiViewModel"
    }

    private var rGameProp: GameProp = GameProp()
    private var rGridData: ReversiGridData = ReversiGridData()
    private val currentPlayer = mutableStateOf(Constants.COLOR_RED)

    init {
        LogUtil.i(TAG, "ReversiViewModel.init")
        mGameProp = rGameProp
        mGridData = rGridData
        super.setProperties()
    }

    fun getCurrentPlayer() = currentPlayer.value

    override fun initGame(bundle: Bundle?) {
        LogUtil.i(TAG, "initGame")
        rGameProp.initializeKeepSetting(WhichGame.REMOVE_BALLS)
        rGridData.initialize()
        setCurrentScore(rGridData.countColor(Constants.COLOR_RED))
        displayGameGridView()
    }

    override fun cellClickListener(i: Int, j: Int) {
        LogUtil.i(TAG, "cellClickListener.($i,$j)")
        if (rGridData.getCellValue(i, j) != 0) return
        val color = currentPlayer.value
        val flips = rGridData.flipsForMove(i, j, color)
        if (flips.isEmpty()) return
        rGridData.backupCells()
        rGridData.placePiece(i, j, color)
        displayGameGridView()

        // switch player
        val nextColor = if (color == Constants.COLOR_RED) Constants.COLOR_BLUE else Constants.COLOR_RED
        currentPlayer.value = nextColor
        val opponentMoves = rGridData.getValidMoves(currentPlayer.value)
        if (opponentMoves.isEmpty()) {
            val myMoves = rGridData.getValidMoves(color)
            if (myMoves.isEmpty()) {
                // game over
                gameOver()
            } else {
                // skip opponent
                currentPlayer.value = color
            }
        }
        setCurrentScore(rGridData.countColor(Constants.COLOR_RED))
    }

    override fun startSavingGame(): Boolean { return false }
    override fun startLoadingGame(): Boolean { return false }

    override fun saveInstanceState(outState: Bundle) {
        outState.putParcelable(com.smile.colorballs_main.constants.Constants.GAME_PROP_TAG, rGameProp)
        outState.putParcelable(com.smile.colorballs_main.constants.Constants.GRID_DATA_TAG, rGridData)
    }

    override fun newGame() {
        rGridData.initialize()
        displayGameGridView()
    }

    override fun undoTheLast() {
        rGridData.undoTheLast()
        displayGameGridView()
    }

    override fun dealWithIsNextBalls(isNextBalls: Boolean) { /* no-op */ }

    override fun release() {
        super.release()
    }
}