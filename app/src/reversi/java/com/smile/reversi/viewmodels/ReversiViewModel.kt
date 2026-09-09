package com.smile.reversi.viewmodels

import android.os.Build
import android.os.Bundle
import android.graphics.Point
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.GameUtil
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import com.smile.reversi.constants.ReversiConstants
import com.smile.reversi.models.ReversiGridData
import com.smile.reversi.presenters.ReversiPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ReversiViewModel(
    private val rPresenter: ReversiPresenter,
    private var playMode: Int
) : BaseViewModel(rPresenter) {

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
    private val currentPlayer = mutableIntStateOf(Constants.COLOR_RED)

    init {
        LogUtil.d(TAG, "ReversiViewModel.init")
        setGameProp(rGameProp)
        setGridData(rGridData)
        super.setProperties()
    }

    fun getCurrentPlayer() = currentPlayer.intValue

    override fun initGame(bundle: Bundle?) {
        LogUtil.d(TAG, "initGame bundle=$bundle")
        setProcessingJob(true)
        val isNewGame = restoreState(bundle)
        displayGameGridView()
        displayEligibleMoves(currentPlayer.intValue)
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

    private fun nextPlayer(): Int {
        return if (currentPlayer.intValue == HUMAN_PLAYER) COMPUTER_PLAYER else HUMAN_PLAYER
    }

    private fun displayEligibleMoves(player: Int) {
        val ball = if (hasNext()) WhichBall.PLUS else WhichBall.NO_BALL
        val validMoves = rGridData.getValidMoves(player)
        for (move in validMoves) {
            gridDataArray[move.x][move.y].value = ColorBallInfo(0, ball)
        }
    }

    private suspend fun placePiece(x: Int, y: Int, color: Int) {
        val flips = rGridData.flipsForMove(x, y, color)
        if (flips.isEmpty()) return
        // Animation for placing the piece: move from south-east to center
        /*
        val steps = 20
        val initialOffset = 20f
        for (step in steps downTo 0) {
            val offset = (step.toFloat() / steps) * initialOffset
            gridDataArray[x][y].value = ColorBallInfo(color, WhichBall.BALL, offsetX = offset, offsetY = offset)
            delay(30L)
        }
        */
        // Animation for placing the piece: move from current player indicator (top-left) to center
        val ballSize = rPresenter.ballImageSizeDp()
        LogUtil.d(TAG, "placePiece.ballSize = $ballSize")
        val steps = 15
        val startX = (1.1f - y) * ballSize + 10f
        val startY = -(1.1f + x) * ballSize
        for (step in steps downTo 0) {
            val ratio = step.toFloat() / steps
            val offX = ratio * startX
            val offY = ratio * startY
            gridDataArray[x][y].value = ColorBallInfo(color, WhichBall.BALL, offsetX = offX, offsetY = offY)
            delay(25L)
        }
        rGridData.setCellValue(x, y, color)
        // Flip animation for flipped pieces
        (0 until 3).forEach { i ->
            for (p in flips) {
                val cellValue = rGridData.getCellValue(p.x, p.y)
                drawOval(p.x, p.y, cellValue)
            }
            delay(200L)
            for (p in flips) {
                drawBall(p.x, p.y, color)
            }
            delay(200L)
        }
        for (p in flips) rGridData.setCellValue(p.x, p.y, color)
    }

    override fun cellClickListener(i: Int, j: Int) {
        // this function is only for HUMAN_PLAYER
        LogUtil.d(TAG, "cellClickListener.($i,$j)")
        if (isProcessingJob()) return
        // If cell already occupied
        if (rGridData.getCellValue(i, j) != 0) {
            return
        }
        setProcessingJob(true)
        val player = if (currentPlayer.intValue == HUMAN_PLAYER) {
            "HUMAN_PLAYER"
        } else {
            "COMPUTER_PLAYER"
        }
        LogUtil.d(TAG, "cellClickListener.player = $player")
        val curPlayer = currentPlayer.intValue
        val humanMoves = rGridData.getValidMoves(curPlayer)
        // If the move is invalid, play uhoh and ignore
        if (!humanMoves.contains(Point(i, j))) {
            setProcessingJob(false)
            if (hasSound()) {
                LogUtil.d(TAG, "cellClickListener.soundPool?.playSound()")
                soundPool?.playSound()
            }
            return
        }
        rGridData.backupCells()
        viewModelScope.launch(Dispatchers.Main) {
            placePiece(i, j, curPlayer)
            displayGameGridView()
            displayEligibleMoves(nextPlayer())
            if (rGridData.isGameOver()) {
                setProcessingJob(false)
                gameOver()
                return@launch
            }
            if (playMode == ReversiConstants.TWO_PLAYERS) {
                currentPlayer.intValue = nextPlayer()
                val validMoves = rGridData.getValidMoves(currentPlayer.intValue)
                if (validMoves.isEmpty()) {
                    // skip to the other player, show a message on screen
                    if (currentPlayer.intValue == HUMAN_PLAYER) {
                        setScreenMessage(rPresenter.redPassStr)
                    } else {
                        setScreenMessage(rPresenter.bluePassStr)
                    }
                    delay(DELAY_FOR_SHOW_PASS)
                    setScreenMessage("")
                    currentPlayer.intValue = nextPlayer()
                    displayEligibleMoves(currentPlayer.intValue)
                }
                setProcessingJob(false)
            } else {
                // Computer's turn - schedule automated move
                scheduleComputerMove()
            }
        }
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
            val gameLevel = getGameLevel()
            LogUtil.d(TAG, "startSavingGame.gameLevel = $gameLevel")
            GameUtil.saveGameLevel(foStream, getGameLevel())
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
            foStream.write(playMode)
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
            LogUtil.d(TAG, "startLoadingGame.gameLevel = $gameLevel")
            bValue = fiStream.read()
            val hasNext = bValue == 1
            setHasSound(hasSound)
            setGameLevel(GameUtil.translateGameLevel(gameLevel))
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
            playMode = fiStream.read()
            // when return -1, then it means the file already reach the end before read()
            if (playMode == -1) playMode = ReversiConstants.PLAY_WIth_AI
            fiStream.close()

            // refresh UI with loaded data
            rGridData.setCellValues(gameCells)
            rGridData.setBackupCells(backupCells)
            displayGameGridView()
            displayEligibleMoves(currentPlayer.intValue)
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
            displayEligibleMoves(currentPlayer.intValue)
        }
    }

    override fun saveScore(playerName: String) {
        // No saving score in this game
    }

    private fun whoWinsMessage() {
        val redCount = rGridData.countPlayer(HUMAN_PLAYER)
        val blueCount = rGridData.countPlayer(COMPUTER_PLAYER)
        saveScoreStr = rPresenter.whoWinsMessage(redCount, blueCount)
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
        // currentPlayer.intValue = COMPUTER_PLAYER
        viewModelScope.launch(Dispatchers.Main) {
            delay(COMPUTER_MOVE_DELAY)
            makeComputerMove()
        }
    }

    private fun makeComputerMove() {
        val logStr = "makeComputerMove"
        setProcessingJob(true)
        LogUtil.d(TAG, logStr)
        viewModelScope.launch(Dispatchers.Main) {
            currentPlayer.intValue = COMPUTER_PLAYER
            val validMoves = rGridData.getValidMoves(currentPlayer.intValue)
            if (validMoves.isEmpty()) {
                LogUtil.d(TAG, "$logStr.skip COMPUTER_PLAYER")
                // skip COMPUTER_PLAYER, show a message on screen
                setScreenMessage(rPresenter.bluePassStr)
                delay(DELAY_FOR_SHOW_PASS)
                setScreenMessage("")
                currentPlayer.intValue = HUMAN_PLAYER
                displayEligibleMoves(currentPlayer.intValue)
                setProcessingJob(false)
                return@launch
            }
            // Choose best move: prioritize corners, then edges, then maximize flips
            withContext(Dispatchers.Default) {
                val bestMove = if (getGameLevel() == Constants.GAME_LEVEL_1)
                    chooseBestMoveBase(validMoves) else chooseBestMove(validMoves)
                rGridData.backupCells()
                withContext(Dispatchers.Main) {
                    // Animating teh move here
                    placePiece(bestMove.x, bestMove.y, COMPUTER_PLAYER)
                    LogUtil.d(TAG, "$logStr.Computer moved to (${bestMove.x}, ${bestMove.y})")
                    displayGameGridView()
                    displayEligibleMoves(nextPlayer())
                    if (rGridData.isGameOver()) {
                        // game over
                        LogUtil.d(TAG, "$logStr.Game is over")
                        setProcessingJob(false)
                        gameOver()
                        return@withContext
                    }
                    // switch back to human player
                    currentPlayer.intValue = HUMAN_PLAYER
                    val humanMoves = rGridData.getValidMoves(currentPlayer.intValue)
                    if (humanMoves.isEmpty()) {
                        // skip HUMAN_PLAYER, show a message on screen
                        LogUtil.d(TAG, "$logStr.skip HUMAN_PLAYER")
                        setScreenMessage(rPresenter.redPassStr)
                        delay(DELAY_FOR_SHOW_PASS)
                        setScreenMessage("")
                        displayEligibleMoves(nextPlayer())
                        scheduleComputerMove()
                    }
                    setProcessingJob(false)
                }
            }
        }
    }

    private fun chooseBestMove(validMoves: List<Point>): Point {
        var bestMove = validMoves[0]
        var bestScore = Float.NEGATIVE_INFINITY

        for (move in validMoves) {
            val score = evaluateMoveWithMinimax(move, COMPUTER_PLAYER, HUMAN_PLAYER, depth = 4)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    private fun evaluateMoveWithMinimax(move: Point, player: Int, opponent: Int, depth: Int ): Float {
        
        val logStr = "evaluateMoveWithMinimax"
        LogUtil.d(TAG, "$logStr.depth = $depth")
        // Backup board state
        val backup = Array(rowCounts) { IntArray(colCounts) }
        for (i in 0 until rowCounts)
            for (j in 0 until colCounts)
                backup[i][j] = rGridData.getCellValue(i, j)
        // Invalid move
        val flips = rGridData.flipsForMove(move.x, move.y, player)
        if (flips.isEmpty()) return Float.NEGATIVE_INFINITY
        // Make move
        rGridData.placePiece(move.x, move.y, player)
        // Static evaluation
        val positionScore = getPositionValue(move.x, move.y)
        val materialScore = flips.size * 10f
        val opponentMoves = rGridData.getValidMoves(opponent)
        val mobilityScore = if (opponentMoves.isEmpty()) 100f else -opponentMoves.size.toFloat()
        var totalScore = positionScore + materialScore + mobilityScore
        // Recursive lookahead using depth
        if (depth > 0 && opponentMoves.isNotEmpty()) {
            var opponentBest = Float.NEGATIVE_INFINITY
            for (opMove in opponentMoves) {
                // evaluate from opponent's perspective; larger is better for opponent
                val oppScore = evaluateMoveWithMinimax(opMove, opponent, player, depth - 1)
                if (oppScore > opponentBest) opponentBest = oppScore
            }
            // opponentBest is good for opponent, so it hurts current player
            totalScore -= opponentBest
        }
        // Restore board
        for (i in 0 until rowCounts)
            for (j in 0 until colCounts)
                rGridData.setCellValue(i, j, backup[i][j])
        return totalScore
    }

    private fun getPositionValue(x: Int, y: Int): Float {
        // Positional weights on the board - same as classic Reversi strategy
        val weights = arrayOf(
            floatArrayOf(400f, -50f, 10f, 5f, 5f, 10f, -50f, 400f),
            floatArrayOf(-50f, -150f, -10f, -5f, -5f, -10f, -150f, -50f),
            floatArrayOf(10f, -10f, 5f, 1f, 1f, 5f, -10f, 10f),
            floatArrayOf(5f, -5f, 1f, 0f, 0f, 1f, -5f, 5f),
            floatArrayOf(5f, -5f, 1f, 0f, 0f, 1f, -5f, 5f),
            floatArrayOf(10f, -10f, 5f, 1f, 1f, 5f, -10f, 10f),
            floatArrayOf(-50f, -150f, -10f, -5f, -5f, -10f, -150f, -50f),
            floatArrayOf(400f, -50f, 10f, 5f, 5f, 10f, -50f, 400f)
        )
        return weights[x][y]
    }

    private fun chooseBestMoveBase(validMoves: List<Point>): Point {
        val corners = listOf(
            Point(0, 0),
            Point(0, 7),
            Point(7, 0),
            Point(7, 7)
        )
        val edges = mutableListOf<Point>()
        val center = mutableListOf<Point>()

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