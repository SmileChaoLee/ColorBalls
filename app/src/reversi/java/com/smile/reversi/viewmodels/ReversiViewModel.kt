package com.smile.reversi.viewmodels

import android.os.Bundle
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichGame
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import com.smile.reversi.models.ReversiGridData
import com.smile.reversi.presenters.ReversiPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class ReversiViewModel(private val rPresenter: ReversiPresenter)
    : BaseViewModel(rPresenter) {

    companion object {
        private const val TAG = "ReversiViewModel"
        private const val COMPUTER_PLAYER = Constants.COLOR_BLUE
        private const val HUMAN_PLAYER = Constants.COLOR_RED
        private const val COMPUTER_MOVE_DELAY = 500L // milliseconds
    }

    private var rGameProp: GameProp = GameProp()
    private var rGridData: ReversiGridData = ReversiGridData()
    private var createNewGameStr = ""
    private val currentPlayer = mutableIntStateOf(Constants.COLOR_RED)

    private val createNewGameText = mutableStateOf("")
    fun getCreateNewGameText() = createNewGameText.value
    fun setCreateNewGameText(text: String) {
        createNewGameText.value = text
    }

    init {
        LogUtil.d(TAG, "ReversiViewModel.init")
        mGameProp = rGameProp
        mGridData = rGridData
        super.setProperties()
    }

    fun getCurrentPlayer() = currentPlayer.intValue

    override fun initGame(bundle: Bundle?) {
        LogUtil.d(TAG, "initGame")
        rGameProp.initializeKeepSetting(WhichGame.REMOVE_BALLS)
        rGridData.initialize()
        // Red always starts
        currentPlayer.intValue = Constants.COLOR_RED
        displayGameGridView()
    }

    override fun cellClickListener(i: Int, j: Int) {
        LogUtil.d(TAG, "cellClickListener.($i,$j)")
        // If game is already over, show dialog (once) and ignore further clicks
        if (rGridData.isGameOver()) {
            handleGameOver()
            return
        }
        // If cell already occupied, play uhoh for human and ignore
        if (rGridData.getCellValue(i, j) != 0) {
            if (currentPlayer.intValue == HUMAN_PLAYER && hasSound()) {
                soundPool?.playSound()
            }
            return
        }
        val color = currentPlayer.intValue
        val flips = rGridData.flipsForMove(i, j, color)
        // If the move is invalid (no flips), play uhoh for human and ignore
        if (flips.isEmpty()) {
            if (currentPlayer.intValue == HUMAN_PLAYER && hasSound()) {
                soundPool?.playSound()
            }
            return
        }
        rGridData.backupCells()
        rGridData.placePiece(i, j, color)
        displayGameGridView()

        // switch player
        val nextColor = if (color == Constants.COLOR_RED) Constants.COLOR_BLUE else Constants.COLOR_RED
        currentPlayer.intValue = nextColor
        val opponentMoves = rGridData.getValidMoves(currentPlayer.intValue)
        if (opponentMoves.isEmpty()) {
            val myMoves = rGridData.getValidMoves(color)
            if (myMoves.isEmpty()) {
                // game over
                handleGameOver()
            } else {
                // skip opponent
                currentPlayer.intValue = color
            }
        } else if (currentPlayer.intValue == COMPUTER_PLAYER) {
            // Computer's turn - schedule automated move
            CoroutineScope(Dispatchers.Main).launch {
                kotlinx.coroutines.delay(COMPUTER_MOVE_DELAY)
                makeComputerMove()
            }
        }
    }

    override fun startSavingGame(): Boolean {
        LogUtil.d(TAG, "startSavingGame")
        rGameProp.isProcessingJob = true
        setScreenMessage(savingGameStr)
        var succeeded = true
        try {
            val foStream = rPresenter.fileOutputStream(Constants.SAVE_REVERSI)
            // save settings
            if (hasSound()) foStream.write(1) else foStream.write(0)
            if (getGameLevel() == Constants.GAME_LEVEL_1) foStream.write(1) else foStream.write(0)
            if (hasNext()) foStream.write(1) else foStream.write(0)
            // save current player
            foStream.write(currentPlayer.intValue)
            // save values on game grid
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(rGridData.getCellValue(i, j))
                }
            }
            // save backupCells
            val backup = rGridData.getBackupCells()
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    foStream.write(backup[i][j])
                }
            }
            foStream.close()
            LogUtil.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: java.io.IOException) {
            succeeded = false
            LogUtil.e(TAG, "startSavingGame.Failed.", ex)
        }
        setScreenMessage("")
        rGameProp.isProcessingJob = false
        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.d(TAG, "startLoadingGame")
        rGameProp.isProcessingJob = true
        setScreenMessage(loadingGameStr)
        var succeeded = true
        try {
            val fiStream = rPresenter.fileInputStream(Constants.SAVE_REVERSI)
            // read game settings
            var bValue = fiStream.read()
            val hasSound = bValue == 1
            bValue = fiStream.read()
            val gameLevel = bValue
            bValue = fiStream.read()
            val hasNext = bValue == 1
            setHasSound(hasSound)
            setGameLevel(gameLevel)
            setHasNext(hasNext)
            // read current player
            val cp = fiStream.read()
            currentPlayer.intValue = cp
            // load values on game grid
            val gameCells = Array(rowCounts) { IntArray(colCounts) }
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    gameCells[i][j] = fiStream.read()
                }
            }
            // reading backupCells
            val backupCells = Array(rowCounts) { IntArray(colCounts) }
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    backupCells[i][j] = fiStream.read()
                }
            }
            fiStream.close()

            // refresh UI with loaded data
            rGridData.setCellValues(gameCells)
            rGridData.setBackupCells(backupCells)
                displayGameGridView()
        } catch (ex: java.io.IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        setScreenMessage("")
        rGameProp.isProcessingJob = false
        return succeeded
    }

    override fun saveInstanceState(outState: Bundle) {
        outState.putParcelable(Constants.GAME_PROP_TAG, rGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, rGridData)
    }

    fun isCreatingNewGame() {
        LogUtil.d(TAG, "isCreatingNewGame")
        mGameAction = Constants.IS_CREATING_GAME
        setCreateNewGameText(createNewGameStr)
    }

    override fun newGame() {
        rGridData.initialize()
        // Reset to starting player (red)
        currentPlayer.intValue = Constants.COLOR_RED
        displayGameGridView()
    }

    override fun undoTheLast() {
        // This game does not support undoing moves.
        // because it is a two-player game.
    }

    private fun handleGameOver() {
        // Compute scores and build message
        LogUtil.d(TAG, "handleGameOver")
        val redCount = rGridData.countColor(Constants.COLOR_RED)
        val blueCount = rGridData.countColor(Constants.COLOR_BLUE)
        val message = when {
            redCount > blueCount -> {
                val diff = redCount - blueCount
                "Red wins by $diff cells (Red: $redCount, Blue: $blueCount)"
            }
            blueCount > redCount -> {
                val diff = blueCount - redCount
                "Blue wins by $diff cells (Blue: $blueCount, Red: $redCount)"
            }
            else -> "It's a tie: Red: $redCount, Blue: $blueCount"
        }
        setSaveScoreTitle(message)
    }

    private fun makeComputerMove() {
        val validMoves = rGridData.getValidMoves(COMPUTER_PLAYER)
        if (validMoves.isEmpty()) {
            LogUtil.d(TAG, "Computer has no valid moves")
            val humanMoves = rGridData.getValidMoves(HUMAN_PLAYER)
            if (humanMoves.isEmpty()) {
                // game over
                handleGameOver()
            } else {
                // skip computer player
                currentPlayer.intValue = HUMAN_PLAYER
                LogUtil.d(TAG, "Computer skipped, human's turn")
            }
            return
        }

        // Choose best move: prioritize corners, then edges, then maximize flips
        val bestMove = chooseBestMove(validMoves)
        rGridData.backupCells()
        rGridData.placePiece(bestMove.x, bestMove.y, COMPUTER_PLAYER)
        displayGameGridView()
        LogUtil.d(TAG, "Computer moved to (${bestMove.x}, ${bestMove.y})")

        // switch back to human player
        currentPlayer.intValue = HUMAN_PLAYER
        val humanMoves = rGridData.getValidMoves(HUMAN_PLAYER)
        if (humanMoves.isEmpty()) {
            val computerMoves = rGridData.getValidMoves(COMPUTER_PLAYER)
            if (computerMoves.isEmpty()) {
                // game over
                handleGameOver()
            } else {
                // skip human player
                currentPlayer.intValue = COMPUTER_PLAYER
                CoroutineScope(Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(COMPUTER_MOVE_DELAY)
                    makeComputerMove()
                }
            }
        }
    }

    private fun chooseBestMove(validMoves: List<android.graphics.Point>): android.graphics.Point {
        val corners = listOf(
            android.graphics.Point(0, 0),
            android.graphics.Point(0, 7),
            android.graphics.Point(7, 0),
            android.graphics.Point(7, 7)
        )
        val edges = mutableListOf<android.graphics.Point>()
        val center = mutableListOf<android.graphics.Point>()

        for (move in validMoves) {
            when {
                move in corners -> return move // Always take a corner
                move.x == 0 || move.x == 7 || move.y == 0 || move.y == 7 -> edges.add(move)
                else -> center.add(move)
            }
        }

        // If edges available, pick the one that flips the most pieces
        if (edges.isNotEmpty()) {
            return edges.maxByOrNull { rGridData.flipsForMove(it.x, it.y, COMPUTER_PLAYER).size } ?: edges[0]
        }

        // Otherwise pick center move that flips the most pieces
        if (center.isNotEmpty()) {
            return center.maxByOrNull { rGridData.flipsForMove(it.x, it.y, COMPUTER_PLAYER).size } ?: center[0]
        }

        // Fallback: pick random valid move
        return validMoves[Random.nextInt(validMoves.size)]
    }

    override fun dealWithIsNextBalls(isNextBalls: Boolean) { /* no-op */ }

    override fun release() {
        super.release()
    }
}