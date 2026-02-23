package com.smile.colorballs.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.os.BundleCompat
import com.smile.colorballs.constants.CbConstants
import com.smile.colorballs.models.CBallGridData
import com.smile.colorballs.presenters.CBallPresenter
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.GameUtil
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.collections.iterator

class CBallViewModel(private val cbPresenter: CBallPresenter)
    : BaseViewModel(cbPresenter) {

    companion object {
        private const val TAG = "CBallViewModel"
    }

    private var cbGameProp: GameProp
    private var cbGridData: CBallGridData
    private val bouncyBallHandler = Handler(Looper.getMainLooper())
    private val movingBallHandler = Handler(Looper.getMainLooper())
    private var gameOverStr = ""

    init {
        LogUtil.i(TAG, "CBallViewModel.init")
        cbGameProp = GameProp()
        cbGridData = CBallGridData(CbConstants.ROW_COUNTS,
            CbConstants.COLUMN_COUNTS)
        mGameProp = cbGameProp
        mGridData = cbGridData
        super.setProperties()
        gameOverStr = cbPresenter.gameOverStr
    }

    override fun cellClickListener(i: Int, j: Int) {
        LogUtil.i(TAG, "cellClickListener.($i, $j)")
        LogUtil.d(TAG, "cellClickListener.isBallBouncing = " +
                "${cbGameProp.isBallBouncing}")
        if (cbGameProp.isProcessingJob) return
        val ballColor = cbGridData.getCellValue(i, j)
        if (ballColor == Constants.COLOR_BARRIER) return
        if (!cbGameProp.isBallBouncing) {
            if (ballColor != 0) {
                if ((cbGameProp.bouncyBallIndexI == -1) &&
                    (cbGameProp.bouncyBallIndexJ == -1)) {
                    cbGameProp.isBallBouncing = true
                    drawBouncyBall(i, j)
                    cbGameProp.bouncyBallIndexI = i
                    cbGameProp.bouncyBallIndexJ = j
                }
            }
        } else {
            val bouncyI = cbGameProp.bouncyBallIndexI
            val bouncyJ = cbGameProp.bouncyBallIndexJ
            if (ballColor == 0) {
                if ((bouncyI >= 0) && (bouncyJ >= 0)) {
                    if (cbGridData.canMoveCellToCell(Point(bouncyI, bouncyJ), Point(i, j))) {
                        cbGameProp.isBallBouncing = false
                        // cancel the bouncy timer
                        stopBouncyAnimation()
                        cbGameProp.bouncyBallIndexI = -1
                        cbGameProp.bouncyBallIndexJ = -1
                        drawBallAlongPath()
                        cbGameProp.undoEnable = true
                    } else {
                        //  make a sound
                        if (hasSound()) {
                            soundPool?.playSound()
                        }
                    }
                }
            } else {
                //  cell is not blank
                if ((bouncyI >= 0) && (bouncyJ >= 0)) {
                    stopBouncyAnimation()
                    drawBall(bouncyI, bouncyJ, cbGridData.getCellValue(bouncyI, bouncyJ))
                    drawBouncyBall(i, j)
                    cbGameProp.bouncyBallIndexI = i
                    cbGameProp.bouncyBallIndexJ = j
                }
            }
        }
    }

    private fun setData(prop: GameProp, gData: CBallGridData) {
        LogUtil.i(TAG, "setData")
        cbGameProp = prop
        cbGridData = gData
        // update mGameProp and mGridData in BaseViewModel
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        LogUtil.i(TAG, "initData")
        cbGameProp.initializeKeepSetting(getWhichGame())
        cbGridData.initialize(getWhichGame())
    }

    override fun initGame(bundle: Bundle?) {
        LogUtil.i(TAG, "initGame = $bundle")
        cbGameProp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        setCurrentScore(cbGameProp.currentScore)
        // displayGameView()
        if (isNewGame) {    // new game
            LogUtil.i(TAG, "initGame.isNewGame")
            displayGameGridView()
            displayGridDataNextCells()
            cbGameProp.isProcessingJob = false
        } else {
            displayGameView()
            // display the original state before changing configuration
            // need to be tested
            if (isShowingMessageDialog()) {
                setScreenMessage(loadingStr)
            }
            if (cbGameProp.isBallMoving) {
                LogUtil.d(TAG, "initGame.cbGameProp.isBallMoving() is true")
                drawBallAlongPath()
            }
            if (isShowingScoreDialog()) {
                LogUtil.d(TAG, "initGame.cbGameProp.isShowingScoreMessage() is true")
                val showScore = ShowScore(
                    cbGridData,
                    cbGridData.getLightLine(),
                    cbGameProp.lastGotScore,
                    cbGameProp.isShowNextBallsAfterBlinking,
                    object : ShowScoreCallback {
                        override fun sCallback() {
                            lastPartOfInitialGame()
                            cbGameProp.isProcessingJob = false
                        }
                    })
                LogUtil.d(TAG, "initGame.showingScoreHandler.post().")
                showingScoreHandler.post(showScore)
            } else {
                lastPartOfInitialGame()
                cbGameProp.isProcessingJob = false
            }
        }
        getAndSetHighestScore() // a coroutine operation
        // cbGameProp.isProcessingJob = false
    }

    private fun restoreState(state: Bundle?): Boolean {
        LogUtil.i(TAG,"restoreState.state = $state")
        var isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: CBallGridData? = null
        state?.let {
            LogUtil.d(TAG,"restoreState.state not null then restore the state")
            gameProp =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GAME_PROP_TAG,
                        GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GRID_DATA_TAG,
                        CBallGridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
        }
        isNewGame = true
        if (gameProp != null && gridData != null) {
            LogUtil.d(TAG, "restoreState.gridData!! = $gridData")
            gridData.apply {
                for (i in 0 until rowCounts) {
                    for (j in 0 until colCounts) {
                        if (getCellValue(i, j) != 0) {
                            // has value, so not a new game
                            isNewGame = false
                            break
                        }
                    }
                }
                if (isNewGame) {
                    LogUtil.d(TAG, "restoreState.CellValues are all 0")
                }
            }
        }
        LogUtil.i(TAG, "restoreState.isNewGame = $isNewGame")
        if (isNewGame) {
            initData()
        } else {
            setData(gameProp!!, gridData!!)
        }

        return isNewGame
    }

    override fun saveInstanceState(outState: Bundle) {
        stopBouncyAnimation()
        movingBallHandler.removeCallbacksAndMessages(null)
        outState.putParcelable(Constants.GAME_PROP_TAG, cbGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, cbGridData)
    }

    fun setHasNext(hasNext: Boolean, isNextBalls: Boolean) {
        setHasNext(hasNext)
        if (isNextBalls) {
            displayNextBallsView()
        }
    }

    override fun undoTheLast() {
        if (!cbGameProp.undoEnable) return
        if (cbGameProp.isProcessingJob) return
        cbGameProp.isProcessingJob = true // started undoing
        cbGridData.undoTheLast()
        stopBouncyAnimation()
        cbGameProp.isBallBouncing = false
        cbGameProp.bouncyBallIndexI = -1
        cbGameProp.bouncyBallIndexJ = -1
        // restore the screen
        displayGameView()
        cbGameProp.currentScore = cbGameProp.undoScore
        setCurrentScore(cbGameProp.currentScore)
        cbGameProp.undoEnable = false
        cbGameProp.isProcessingJob = false // finished
    }

    override fun newGame() {
        // creating a new game
        stopBouncyAnimation()
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    override fun startSavingGame(): Boolean {
        LogUtil.i(TAG, "startSavingGame")
        cbGameProp.isProcessingJob = true
        setScreenMessage(savingGameStr)

        var succeeded = true
        try {
            val fileName = GameUtil.getSaveFileName(getWhichGame())
            val foStream = cbPresenter.fileOutputStream(fileName)
            // save settings
            LogUtil.d(TAG, "startSavingGame.hasSound = " + hasSound())
            if (hasSound()) foStream.write(1) else foStream.write(0)
            LogUtil.d(TAG, "startSavingGame.isEasyLevel = " + getGameLevel())
            if (getGameLevel() == Constants.GAME_LEVEL_1) foStream.write(1)
            else foStream.write(0)
            LogUtil.d(TAG, "startSavingGame.hasNext = " + hasNext())
            if (hasNext()) foStream.write(1) else foStream.write(0)
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            LogUtil.d(TAG, "startSavingGame.ballNumOneTime = " + CbConstants.BALL_NUM_ONE_TIME)
            foStream.write(CbConstants.BALL_NUM_ONE_TIME)
            for ((_, value) in cbGridData.getNextCellIndices()) {
                LogUtil.d(TAG, "startSavingGame.nextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            var sz = cbGridData.getNextCellIndices().size
            (sz until Constants.NUM_BALLS_USED_DIFF).forEach { _ ->
                LogUtil.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            LogUtil.d(TAG, "startSavingGame.getNextCellIndices.size() = $sz")
            foStream.write(sz)
            for ((key) in cbGridData.getNextCellIndices()) {
                LogUtil.d(TAG, "startSavingGame.nextCellIndices.getKey().x = " + key.x)
                LogUtil.d(TAG, "startSavingGame.nextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            LogUtil.d(
                TAG,"startSavingGame.getUndoNextCellIndices().size() = "
                    + cbGridData.getUndoNextCellIndices().size)
            foStream.write(cbGridData.getUndoNextCellIndices().size)
            for ((key) in cbGridData.getUndoNextCellIndices()) {
                LogUtil.d(TAG, "startSavingGame.undoNextCellIndices.getKey().x = " + key.x)
                LogUtil.d(TAG, "startSavingGame.undoNextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            // save values on 9x9 grid
            for (i in 0 until cbGridData.rowCounts) {
                for (j in 0 until cbGridData.colCounts) {
                    LogUtil.d(TAG,"startSavingGame.gridData.getCellValue(i, j) = "
                            + cbGridData.getCellValue(i, j))
                    foStream.write(cbGridData.getCellValue(i, j))
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(cbGameProp.currentScore).array()
            LogUtil.d(TAG, "startSavingGame.scoreByte = $scoreByte")
            foStream.write(scoreByte)
            // save undoEnable
            LogUtil.d(TAG, "startSavingGame.isUndoEnable = " + cbGameProp.undoEnable)
            // can undo or no undo
            if (cbGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            LogUtil.d(TAG, "startSavingGame.ballNumOneTime = " + CbConstants.BALL_NUM_ONE_TIME)
            foStream.write(CbConstants.BALL_NUM_ONE_TIME)
            // save undoNextBalls
            for ((_, value) in cbGridData.getUndoNextCellIndices()) {
                LogUtil.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            sz = cbGridData.getUndoNextCellIndices().size
            (sz until Constants.NUM_BALLS_USED_DIFF).forEach { _ ->
                LogUtil.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            // save backupCells
            for (i in 0 until cbGridData.rowCounts) {
                for (j in 0 until cbGridData.colCounts) {
                    LogUtil.d(TAG,"startSavingGame.gridData.getBackupCells()[i][j] = "
                            + cbGridData.getBackupCells()[i][j])
                    foStream.write(cbGridData.getBackupCells()[i][j])
                }
            }
            val undoScoreByte = ByteBuffer.allocate(4).putInt(cbGameProp.undoScore).array()
            LogUtil.d(TAG, "startSavingGame.undoScoreByte = $undoScoreByte")
            foStream.write(undoScoreByte)
            foStream.close()
            // end of writing
            LogUtil.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            LogUtil.e(TAG, "startSavingGame.Failed.", ex)
        }
        setScreenMessage("")
        LogUtil.d(TAG, "startSavingGame.Finished")
        cbGameProp.isProcessingJob = false

        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.i(TAG, "startLoadingGame")
        cbGameProp.isProcessingJob = true
        setScreenMessage(loadingGameStr)

        var succeeded = true
        val hasSound: Boolean
        val gameLevel: Int
        val hasNext: Boolean
        var ballNumOneTime: Int
        val nextBalls = IntArray(Constants.NUM_BALLS_USED_DIFF)
        val gameCells = Array(cbGridData.rowCounts) {
            IntArray(cbGridData.colCounts) }
        val cScore: Int
        val isUndoEnable: Boolean
        val undoNextBalls = IntArray(Constants.NUM_BALLS_USED_DIFF)
        val backupCells = Array(cbGridData.rowCounts) {
            IntArray(cbGridData.colCounts) }
        var unScore = cbGameProp.undoScore
        try {
            // clear nextCellIndices and undoNextCellIndices
            cbGridData.setNextCellIndices(HashMap())
            cbGridData.setUndoNextCellIndices(HashMap())
            LogUtil.d(TAG, "startLoadingGame.Creating inputFile")
            val fileName = GameUtil.getSaveFileName(getWhichGame())
            // File inputFile = new File(mContext.getFilesDir(), savedGameFileName);
            // long fileSizeInByte = inputFile.length();
            // LogUtil.d(TAG, "startLoadingGame.File size = " + fileSizeInByte);
            // FileInputStream fiStream = new FileInputStream(inputFile);
            val fiStream = cbPresenter.fileInputStream(fileName)
            LogUtil.d(TAG, "startLoadingGame.available() = " + fiStream.available())
            LogUtil.d(TAG, "startLoadingGame.getChannel().size() = " + fiStream.channel.size())
            // game sound
            var bValue = fiStream.read()
            hasSound = bValue == 1
            LogUtil.d(TAG, "startLoadingGame.hasSound = $hasSound")
            // game level
            bValue = fiStream.read()
            gameLevel = bValue
            LogUtil.d(TAG, "startLoadingGame.isEasyLevel = $gameLevel")
            // next balls
            bValue = fiStream.read()
            hasNext = bValue == 1
            LogUtil.d(TAG, "startLoadingGame.hasNextBall = $hasNext")
            ballNumOneTime = fiStream.read()
            LogUtil.i(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
            for (i in 0 until Constants.NUM_BALLS_USED_DIFF) {
                nextBalls[i] = fiStream.read()
                LogUtil.d(TAG, "startLoadingGame.nextCellIndices.cell.getColor() = " + nextBalls[i])
            }
            val nextCellIndicesSize = fiStream.read()
            LogUtil.d(TAG, "startLoadingGame.getNextCellIndices.size() = $nextCellIndicesSize")
            (0 until nextCellIndicesSize).forEach { _ ->
                val x = fiStream.read()
                val y = fiStream.read()
                LogUtil.d(TAG, "startLoadingGame.nextCellIndices.getKey().x = $x")
                LogUtil.d(TAG, "startLoadingGame.nextCellIndices.getKey().y = $y")
                cbGridData.addNextCellIndices(Point(x, y))
            }
            val undoNextCellIndicesSize = fiStream.read()
            LogUtil.d(
                TAG,"startLoadingGame.getUndoNextCellIndices.size() = " +
                    "$undoNextCellIndicesSize")
            (0 until undoNextCellIndicesSize).forEach { _ ->
                val x = fiStream.read()
                val y = fiStream.read()
                LogUtil.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = $x")
                LogUtil.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = $y")
                cbGridData.addUndoNextCellIndices(Point(x, y))
            }
            // load values on 9x9 grid
            for (i in 0 until cbGridData.rowCounts) {
                for (j in 0 until cbGridData.colCounts) {
                    gameCells[i][j] = fiStream.read()
                    LogUtil.d(TAG, "startLoadingGame.gridData.getCellValue(i, j) = " + gameCells[i][j])
                }
            }
            // reading current score
            val scoreByte = ByteArray(4)
            fiStream.read(scoreByte)
            LogUtil.d(TAG, "startLoadingGame.scoreByte = $scoreByte")
            cScore = ByteBuffer.wrap(scoreByte).getInt()
            // reading undoEnable
            bValue = fiStream.read()
            isUndoEnable = bValue == 1
            LogUtil.d(TAG, "startLoadingGame.isUndoEnable = $isUndoEnable")
            if (isUndoEnable) {
                ballNumOneTime = fiStream.read()
                LogUtil.d(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
                for (i in 0 until Constants.NUM_BALLS_USED_DIFF) {
                    undoNextBalls[i] = fiStream.read()
                    LogUtil.d(
                        TAG,"startLoadingGame.undoNextCellIndices.getValue() = "
                            + undoNextBalls[i])
                }
                // save backupCells
                for (i in 0 until cbGridData.rowCounts) {
                    for (j in 0 until cbGridData.colCounts) {
                        backupCells[i][j] = fiStream.read()
                        LogUtil.d(
                            TAG,"startLoadingGame.gridData.getBackupCells()[i][j] = "
                                + backupCells[i][j])
                    }
                }
                val undoScoreByte = ByteArray(4)
                fiStream.read(undoScoreByte)
                LogUtil.d(TAG, "startLoadingGame.undoScoreByte = $undoScoreByte")
                unScore = ByteBuffer.wrap(undoScoreByte).getInt()
            }
            fiStream.close()
            // refresh Main UI with loaded data
            setHasSound(hasSound)
            setGameLevel(gameLevel)
            setHasNext(hasNext, false)
            var kk = 0
            for (entry in cbGridData.getNextCellIndices().entries) {
                entry.setValue(nextBalls[kk++])
            }
            cbGridData.setCellValues(gameCells)
            cbGameProp.currentScore = cScore
            cbGameProp.undoEnable = isUndoEnable
            kk = 0
            for (entry in cbGridData.getUndoNextCellIndices().entries) {
                entry.setValue(undoNextBalls[kk++])
            }
            cbGridData.setBackupCells(backupCells)
            cbGameProp.undoScore = unScore
            // start update UI
            setCurrentScore(cbGameProp.currentScore)
            LogUtil.d(TAG, "startLoadingGame.starting displayGameView().")
            displayGameView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        setScreenMessage("")
        cbGameProp.isProcessingJob = false

        return succeeded
    }

    override fun release() {
        super.release()
        stopBouncyAnimation()
        movingBallHandler.removeCallbacksAndMessages(null)
    }

    override fun dealWithIsNextBalls(isNextBalls: Boolean) {
        LogUtil.d(TAG, "dealWithIsNextBalls.isNextBalls = $isNextBalls")
        if (isNextBalls) {
            displayNextColorBalls()
        } else {
            displayNextBallsView()
        }
    }

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }

        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2

        /*
        val numBalls = intArrayOf(0, 0, 0, 0, 0, 0)
        for (point in linkedLine) {
            when (cbGridData.getCellValue(point.x, point.y)) {
                Constants.COLOR_RED -> numBalls[0]++
                Constants.COLOR_GREEN -> numBalls[1]++
                Constants.COLOR_BLUE -> numBalls[2]++
                Constants.COLOR_MAGENTA -> numBalls[3]++
                Constants.COLOR_YELLOW -> numBalls[4]++
                Constants.COLOR_CYAN -> numBalls[5]++
                else -> {}
            }
        }
        val minScore = 5
        var totalScore = 0
        for (numBall in numBalls) {
            if (numBall >= 5) {
                var score = minScore
                val extraBalls = numBall - minScore
                if (extraBalls > 0) {
                    // greater than 5 balls
                    val rate = 2
                    for (i in 1..extraBalls) {
                        // rate = 2;   // added on 2018-10-02
                        score += i * rate
                    }
                }
                totalScore += score
            }
        }
        if (getGameLevel() != Constants.GAME_LEVEL_1) {
            // difficult level
            totalScore *= 2 // double of easy level
        }
        return totalScore
        */

        val numBalls = linkedLine.size
        val minBalls = 5
        var score = 5
        val extraBalls = numBalls - minBalls
        if (extraBalls > 0) {
            // greater than 5 balls
            val rate  = 2
            for (i in 1..extraBalls) {
                score += i * rate ;
            }
        }
        if (getGameLevel() != Constants.GAME_LEVEL_1) {
            // difficult level
            score *= 2 // double of easy level
        }

        return score;
    }

    private fun drawBouncyBall(i: Int, j: Int) {
        val color = cbGridData.getCellValue(i, j)
        LogUtil.d(TAG, "drawBouncyBall.($i, $j), color = $color")
        var whichBall= WhichBall.BALL
        object : Runnable {
            override fun run() {
                if (whichBall == WhichBall.BALL) drawBall(i, j, color)
                else drawOval(i, j, color)
                whichBall = if (whichBall == WhichBall.BALL) WhichBall.OVAL_BALL
                else WhichBall.BALL
                bouncyBallHandler.postDelayed(this, 200)
            }
        }.also { bouncyBallHandler.post(it) }
    }

    private fun stopBouncyAnimation() {
        bouncyBallHandler.removeCallbacksAndMessages(null)
    }

    private fun drawNextBall(i: Int, j: Int, color: Int) {
        LogUtil.i(TAG, "drawNextBall.($i, $j), color = $color")
        val trueColor = if (hasNext()) color else 0
        LogUtil.d(TAG, "drawNextBall.($i, $j), trueColor = $trueColor")
        gridDataArray[i][j].value = ColorBallInfo(trueColor, WhichBall.NEXT_BALL)
    }

    private fun displayNextBallsView() {
        // display the view of next balls
        LogUtil.i(TAG, "displayNextBallsView")
        try {
            for ((key, value) in cbGridData.getNextCellIndices()) {
                LogUtil.d(TAG, "displayNextBallsView.color = $value")
                drawNextBall(key.x, key.y, value)
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, "displayNextBallsView.Exception: ", ex)
            ex.printStackTrace()
        }
    }

    private fun displayNextColorBalls() {
        cbGridData.randThreeCells()
        if (cbGridData.isGameOver()) {
            // no vacant, so game over
            gameOver()
            return
        }
        //   display the balls on the nextBallsView
        displayNextBallsView()
    }

    private fun displayGridDataNextCells() {
        LogUtil.i(TAG, "displayGridDataNextCells")
        var n1: Int
        var n2: Int
        var hasMoreFive = false
        val linkedPoint = HashSet<Point>()
        for ((key, value) in cbGridData.getNextCellIndices()) {
            n1 = key.x
            n2 = key.y
            LogUtil.d(TAG, "displayGridDataNextCells.($n1, $n2), color = $value")
            cbGridData.setCellValue(n1, n2, value)
            drawBall(n1, n2, cbGridData.getCellValue(n1, n2))
            if (cbGridData.moreThan5VerHorDia(n1, n2)) {
                hasMoreFive = true
                for (point in cbGridData.getLightLine()) {
                    if (!linkedPoint.contains(point)) {
                        linkedPoint.add(Point(point))
                    }
                }
            }
        }

        if (hasMoreFive) {
            cbGridData.setLightLine(linkedPoint) // added on 2020-07-13
            cbGameProp.lastGotScore = calculateScore(cbGridData.getLightLine())
            cbGameProp.undoScore = cbGameProp.currentScore
            cbGameProp.currentScore += cbGameProp.lastGotScore
            setCurrentScore(cbGameProp.currentScore)
            val showScore = ShowScore(
                cbGridData,
                cbGridData.getLightLine(),
                cbGameProp.lastGotScore,
                true, object : ShowScoreCallback {
                    override fun sCallback() {
                        LogUtil.d(TAG, "ShowScoreCallback.sCallback.Do nothing.")
                    }
                })
            LogUtil.d(TAG, "displayGridDataNextCells.post(showScore)")
            showingScoreHandler.post(showScore)
        } else {
            displayNextColorBalls()
        }
    }

    private fun displayGameView() {
        // display the 9 x 9 game view
        LogUtil.i(TAG, "displayGameView")
        displayGameGridView()
        // display the view of next balls
        displayNextBallsView()
    }

    private fun drawBallAlongPath() {
        val sizeOfPath = cbGridData.mPathPoint.size
        if (sizeOfPath == 0) {
            LogUtil.w(TAG, "drawBallAlongPath.sizeOfPathPoint == 0")
            return
        }
        cbGameProp.isBallMoving = true
        cbGameProp.isProcessingJob = true

        val beginI = cbGridData.mPathPoint[sizeOfPath - 1].x
        val beginJ = cbGridData.mPathPoint[sizeOfPath - 1].y
        LogUtil.d(TAG, "drawBallAlongPath.beginI = $beginI, beginJ = $beginJ")
        val targetI = cbGridData.mPathPoint[0].x // the target point
        val targetJ = cbGridData.mPathPoint[0].y // the target point
        LogUtil.d(TAG, "drawBallAlongPath.targetI = $targetI, targetJ = $targetJ")
        val color = cbGridData.getCellValue(beginI, beginJ)
        LogUtil.d(TAG, "drawBallAlongPath.color = $color")

        val tempList = ArrayList(cbGridData.mPathPoint)
        val runnablePath: Runnable = object : Runnable {
            var ballYN: Boolean = true
            var countDown: Int = tempList.size * 2 - 1
            @Synchronized
            override fun run() {
                movingBallHandler.removeCallbacksAndMessages(null)
                if (countDown >= 2) {   // eliminate start point
                    val i = countDown / 2
                    drawBall(tempList[i].x, tempList[i].y, if (ballYN) color else 0)
                    ballYN = !ballYN
                    countDown--
                    movingBallHandler.postDelayed(this, 20)
                } else {
                    clearCell(beginI, beginJ) // blank the original cell. Added on 2020-09-16
                    cbGridData.setCellValue(targetI, targetJ, color)
                    drawBall(targetI, targetJ, color)
                    //  check if there are more than five balls
                    //  with same color connected together
                    if (cbGridData.moreThan5VerHorDia(targetI, targetJ)) {
                        cbGameProp.lastGotScore = calculateScore(cbGridData.getLightLine())
                        cbGameProp.undoScore = cbGameProp.currentScore
                        cbGameProp.currentScore += cbGameProp.lastGotScore
                        setCurrentScore(cbGameProp.currentScore)
                        LogUtil.d(TAG, "drawBallAlongPath.showScore")
                        val showScore = ShowScore(
                            cbGridData,
                            cbGridData.getLightLine(),
                            cbGameProp.lastGotScore,
                            false, object : ShowScoreCallback {
                                override fun sCallback() {
                                    LogUtil.d(TAG, "drawBallAlongPath.ShowScoreCallback.sCallback")
                                    cbGameProp.isBallMoving = false
                                    cbGameProp.isProcessingJob = false
                                }
                            })
                        LogUtil.d(TAG, "drawBallAlongPath.showingScoreHandler.post")
                        showingScoreHandler.post(showScore)
                    } else {
                        cbGridData.regenerateNextCellIndices(Point(targetI, targetJ))
                        LogUtil.d(TAG, "drawBallAlongPath.run().displayGridDataNextCells")
                        displayGridDataNextCells() // has a problem
                        LogUtil.d(TAG, "drawBallAlongPath.run() finished.")
                        cbGameProp.isBallMoving = false
                        cbGameProp.isProcessingJob = false
                    }
                }
            }
        }
        LogUtil.i(TAG, "drawBallAlongPath.movingBallHandler.post")
        movingBallHandler.post(runnablePath)
    }

    private inner class ShowScore_old(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int, val isNextBalls: Boolean,
        val callback: ShowScoreCallback
    ): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            LogUtil.i(TAG, "ShowScore")
            pointSet = HashSet(linkedPoint)
            cbGameProp.isShowNextBallsAfterBlinking = isNextBalls
            setShowingScoreDialog(true)
        }

        @Synchronized
        private fun onProgressUpdate(status: Int) {
            when (status) {
                0 -> for (item in pointSet) {
                    drawBall(item.x, item.y, cbGridData.getCellValue(item.x, item.y))
                }
                1 -> for (item in pointSet) {
                    drawOval(item.x, item.y, cbGridData.getCellValue(item.x, item.y))
                }
                2 -> for (item in pointSet) {
                    drawFirework(item.x, item.y)
                }
                3 -> {
                    setScreenMessage(lastGotScore.toString())
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, cbGridData.getCellValue(item.x, item.y))
                    }
                    if (isNextBalls) {
                        LogUtil.d(TAG, "ShowScore.onProgressUpdate.displayNextColorBalls")
                        displayNextColorBalls()
                    } else {
                        LogUtil.d(TAG, "ShowScore.onProgressUpdate.displayNextBallsView")
                        displayNextBallsView()
                    }
                }
                4 -> {
                    LogUtil.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                    setScreenMessage("")
                    setShowingScoreDialog(false)
                }
                else -> {}
            }
        }

        @Synchronized
        override fun run() {
            val twinkleCountDown = 5
            mCounter++
            LogUtil.d(TAG, "ShowScore.run().mCounter = $mCounter")
            showingScoreHandler.removeCallbacksAndMessages(null)
            if (mCounter <= twinkleCountDown) {
                val md = mCounter % 2 // modulus
                onProgressUpdate(md)
                showingScoreHandler.postDelayed(this, 100)
            } else {
                when (mCounter) {
                    twinkleCountDown + 1 -> {
                        onProgressUpdate(2) // show the flash
                        showingScoreHandler.postDelayed(this, 600)
                    }
                    twinkleCountDown + 2 -> {
                        onProgressUpdate(3) // show score
                        showingScoreHandler.postDelayed(this, 500)
                    }
                    else -> {
                        onProgressUpdate(4) // dismiss showing message
                        callback.sCallback()
                    }
                }
            }
        }
    }
}