package com.smile.reversi.viewmodels

import android.os.Build
import android.os.Bundle
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import com.smile.reversi.models.ReversiGridData
import com.smile.reversi.presenters.ReversiPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class ReversiViewModel(private val rPresenter: ReversiPresenter)
    : BaseViewModel(rPresenter) {

    companion object {
        private const val TAG = "ReversiViewModel"
        private const val COMPUTER_PLAYER = ReversiGridData.COMPUTER_PLAYER
        private const val HUMAN_PLAYER = ReversiGridData.HUMAN_PLAYER
        private const val COMPUTER_MOVE_DELAY = 500L // milliseconds
        private const val DELAY_FOR_SHOW_PASS = 3000L // milliseconds
        private const val CURRENT_PLAYER_TAG = "CurrentPlayer"
        private const val SAVE_SCORE_STR_TAG = "SaveScoreStr"
    }

    private var rGameProp: GameProp = GameProp()
    private var rGridData: ReversiGridData = ReversiGridData()
    private var createNewGameStr = ""
    private val currentPlayer = mutableIntStateOf(Constants.COLOR_RED)

    private val createNewGameText = mutableStateOf("")
    init {
        LogUtil.d(TAG, "ReversiViewModel.init")
        setGameProp(rGameProp)
        setGridData(rGridData)
        super.setProperties()
        createNewGameStr = rPresenter.createNewGameStr
    }

    fun getCurrentPlayer() = currentPlayer.intValue

    override fun initGame(bundle: Bundle?) {
        LogUtil.d(TAG, "initGame bundle=$bundle")
        setProcessingJob(true)
        val isNewGame = restoreState(bundle)
        displayGameGridView()
        if (!isNewGame) {
            lastPartOfInitialGame()
        }
        setProcessingJob(false)
    }

    private fun restoreState(state: Bundle?): Boolean {
        LogUtil.d(TAG, "restoreState.state = $state")
        var isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: ReversiGridData? = null
        state?.let {
            gameProp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                it.getParcelable(Constants.GAME_PROP_TAG, GameProp::class.java)
            else it.getParcelable(Constants.GAME_PROP_TAG)

            gridData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                it.getParcelable(Constants.GRID_DATA_TAG, ReversiGridData::class.java)
            else it.getParcelable(Constants.GRID_DATA_TAG) as? ReversiGridData

            currentPlayer.intValue = it.getInt(CURRENT_PLAYER_TAG, Constants.COLOR_RED)
            saveScoreStr = it.getString(SAVE_SCORE_STR_TAG, saveScoreStr)

        }

        isNewGame = true
        if (gameProp != null && gridData != null) {
            isNewGame = false
        }

        LogUtil.d(TAG, "restoreState.isNewGame = $isNewGame")
        if (isNewGame) {
            initData()
        } else {
            setData(gameProp!!, gridData!!)
        }

        return isNewGame
    }

    private fun setData(prop: GameProp, gData: ReversiGridData) {
        LogUtil.d(TAG, "setData")
        rGameProp = prop
        rGridData = gData
        // update mGameProp and mGridData in BaseViewModel
        setGameProp(prop)
        setGridData(gData)
    }

    private fun initData() {
        LogUtil.d(TAG, "initData")
        rGameProp.initializeKeepSetting(getWhichGame())
        rGridData.initialize()
        currentPlayer.intValue = Constants.COLOR_RED
    }

    private fun displayEligibleMoves() {
        val ball = if (hasNext()) WhichBall.PLUS else WhichBall.NO_BALL
        // Show eligible moves for the human player
        val validMoves = rGridData.getValidMoves(HUMAN_PLAYER)
        for (move in validMoves) {
            gridDataArray[move.x][move.y].value = ColorBallInfo(0, ball)
        }
    }

    override fun displayGameGridView() {
        super.displayGameGridView()
        displayEligibleMoves()
    }

    override fun cellClickListener(i: Int, j: Int) {
        // this function is only for HUMAN_PLAYER
        LogUtil.d(TAG, "cellClickListener.($i,$j)")
        if (isProcessingJob()) return
        // If cell already occupied
        if (rGridData.getCellValue(i, j) != 0) {
            return
        }
        val player = if (currentPlayer.intValue == HUMAN_PLAYER) {
            "HumanHUMAN_PLAYER"
        } else {
            "COMPUTER_PLAYER"
        }
        LogUtil.d(TAG, "cellClickListener.player = $player")
        val curPlayer = currentPlayer.intValue  // it must be HUMAN_PLAYER
        val flips = rGridData.flipsForMove(i, j, curPlayer)
        // If the move is invalid (no flips), play uhoh for human and ignore
        if (flips.isEmpty()) {
            if (hasSound()) {
                soundPool?.playSound()
            }
            return
        }
        // rGridData.backupCells()  // No need to backup for undo, since undo is not supported in this game
        rGridData.placePiece(i, j, curPlayer)
        displayGameGridView()
        setProcessingJob(false)
        if (rGridData.isGameOver()) {
            gameOver()
            return
        }
        // Computer's turn - schedule automated move
        scheduleComputerMove()
    }

    override fun startSavingGame(): Boolean {
        LogUtil.d(TAG, "startSavingGame")
        setProcessingJob(true)
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
        setProcessingJob(false)
        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.d(TAG, "startLoadingGame")
        setProcessingJob(true)
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
        setProcessingJob(false)
        return succeeded
    }

    override fun saveInstanceState(outState: Bundle) {
        outState.putParcelable(Constants.GAME_PROP_TAG, rGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, rGridData)
        outState.putInt(CURRENT_PLAYER_TAG, currentPlayer.intValue)
        outState.putString(SAVE_SCORE_STR_TAG, saveScoreStr)
    }

    fun setHasNext(hasNext: Boolean, isNextBalls: Boolean) {
        setHasNext(hasNext)
        if (isNextBalls) {
            displayEligibleMoves()
        }
    }

    override fun saveScore(playerName: String) {
        // No saving score in this game
    }

    private fun whoWinsMessage() {
        val redCount = rGridData.countPlayer(HUMAN_PLAYER)
        val blueCount = rGridData.countPlayer(COMPUTER_PLAYER)
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
        saveScoreStr = message
    }

    override fun quitGame() {
        whoWinsMessage()
        super.quitGame()
    }

    override fun newGame() {
        LogUtil.d(TAG, "newGame.called by gameOver()")
        whoWinsMessage()
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    override fun undoTheLast() {
        // This game does not support undoing moves.
        // because it is a two-player game.
    }

    private fun scheduleComputerMove() {
        LogUtil.d(TAG, "scheduleComputerMove")
        setProcessingJob(true)
        currentPlayer.intValue = COMPUTER_PLAYER
        viewModelScope.launch(Dispatchers.Main) {
            delay(COMPUTER_MOVE_DELAY)
            makeComputerMove()
            setProcessingJob(false)
        }
    }

    private fun makeComputerMove() {
        val logStr = "makeComputerMove"
        LogUtil.d(TAG, logStr)
        val validMoves = rGridData.getValidMoves(COMPUTER_PLAYER)
        if (validMoves.isEmpty()) {
            LogUtil.d(TAG, "$logStr.skip COMPUTER_PLAYER")
            // skip COMPUTER_PLAYER, show a message on screen
            viewModelScope.launch(Dispatchers.Main) {
                setScreenMessage("Blue passed")
                currentPlayer.intValue = HUMAN_PLAYER
                delay(DELAY_FOR_SHOW_PASS)
                setScreenMessage("")
            }
            return
        }
        // Choose best move: prioritize corners, then edges, then maximize flips
        val bestMove = chooseBestMove(validMoves)
        rGridData.backupCells()
        rGridData.placePiece(bestMove.x, bestMove.y, COMPUTER_PLAYER)
        LogUtil.d(TAG, "$logStr.Computer moved to (${bestMove.x}, ${bestMove.y})")
        displayGameGridView()
        if (rGridData.isGameOver()) {
            // game over
            LogUtil.d(TAG, "$logStr.Game is over")
            gameOver()
            return
        }
        // switch back to human player
        currentPlayer.intValue = HUMAN_PLAYER
        val humanMoves = rGridData.getValidMoves(HUMAN_PLAYER)
        if (humanMoves.isEmpty()) {
            // skip HUMAN_PLAYER, show a message on screen
            LogUtil.d(TAG, "$logStr.skip HUMAN_PLAYER")
            viewModelScope.launch(Dispatchers.Main) {
                setScreenMessage("Red pass")
                delay(DELAY_FOR_SHOW_PASS)
                setScreenMessage("")
                scheduleComputerMove()
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