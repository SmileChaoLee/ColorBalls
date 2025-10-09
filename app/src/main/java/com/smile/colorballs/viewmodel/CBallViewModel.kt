package com.smile.colorballs.viewmodel

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.BundleCompat
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.models.CBallGridData
import com.smile.colorballs.models.ColorBallInfo
import com.smile.colorballs.presenters.CBallPresenter
import com.smile.colorballs.tools.Utils
import java.io.IOException
import java.nio.ByteBuffer

class CBallViewModel(private val cbPresenter: CBallPresenter)
    : BaseViewModel(cbPresenter) {

    private var cbGameProp: GameProp
    private var cbGridData: CBallGridData
    private val bouncyBallHandler = Handler(Looper.getMainLooper())
    private val movingBallHandler = Handler(Looper.getMainLooper())
    private val showingScoreHandler = Handler(Looper.getMainLooper())
    private var gameOverStr = ""

    init {
        Log.d(TAG, "CBallViewModel.init")
        cbGameProp = GameProp()
        cbGridData = CBallGridData()
        mGameProp = cbGameProp
        mGridData = cbGridData
        super.setProperties()
        gameOverStr = cbPresenter.gameOverStr
    }

    override fun cellClickListener(i: Int, j: Int) {
        Log.d(TAG, "cellClickListener.($i, $j)")
        Log.d(TAG, "cellClickListener.isBallBouncing = " +
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
                            soundPool.playSound()
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
        Log.d(TAG, "setData")
        cbGameProp = prop
        cbGridData = gData
        // update mGameProp and mGridData in BaseViewModel
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        Log.d(TAG, "initData")
        cbGameProp.initializeKeepSetting(getWhichGame())
        cbGridData.initialize(getWhichGame())
    }

    override fun initGame(bundle: Bundle?) {
        Log.d(TAG, "initGame = $bundle")
        cbGameProp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        setCurrentScore(cbGameProp.currentScore)
        // displayGameView()
        if (isNewGame) {    // new game
            Log.d(TAG, "initGame.isNewGame")
            displayGameGridView()
            displayGridDataNextCells()
        } else {
            displayGameView()
            // display the original state before changing configuration
            // need to be tested
            if (isShowingMessageDialog()) {
                setScreenMessage(loadingStr)
            }
            if (cbGameProp.isBallMoving) {
                Log.d(TAG, "initGame.cbGameProp.isBallMoving() is true")
                drawBallAlongPath()
            }
            if (isShowingScoreDialog()) {
                Log.d(TAG, "initGame.cbGameProp.isShowingScoreMessage() is true")
                val showScore = ShowScore(
                    cbGridData.getLightLine(), cbGameProp.lastGotScore,
                    cbGameProp.isShowNextBallsAfterBlinking,
                    object : ShowScoreCallback {
                        override fun sCallback() {
                            lastPartOfInitialGame()
                        }
                    })
                Log.d(TAG, "initGame.showingScoreHandler.post().")
                showingScoreHandler.post(showScore)
            } else {
                lastPartOfInitialGame()
            }
        }
        getAndSetHighestScore() // a coroutine operation
        cbGameProp.isProcessingJob = false
    }

    private fun restoreState(state: Bundle?): Boolean {
        var isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: CBallGridData? = null
        state?.let {
            Log.d(TAG,"restoreState.state not null then restore the state")
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
            Log.d(TAG, "restoreState.gridData!! = $gridData")
            gridData.apply {
                for (i in 0 until Constants.ROW_COUNTS) {
                    for (j in 0 until Constants.ROW_COUNTS) {
                        if (getCellValue(i, j) != 0) {
                            // has value, so not a new game
                            isNewGame = false
                            break
                        }
                    }
                }
                if (isNewGame) {
                    Log.d(TAG, "restoreState.CellValues are all 0")
                }
            }
        }
        Log.d(TAG, "restoreState.isNewGame = $isNewGame")
        if (isNewGame) {
            initData()
        } else {
            setData(gameProp!!, gridData!!)
        }

        return isNewGame
    }

    override fun saveInstanceState(outState: Bundle) {
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
        Log.d(TAG, "startSavingGame")
        cbGameProp.isProcessingJob = true
        setScreenMessage(savingGameStr)

        var succeeded = true
        try {
            val fileName = Utils.getSaveFileName(getWhichGame())
            val foStream = cbPresenter.fileOutputStream(fileName)
            // save settings
            Log.d(TAG, "startSavingGame.hasSound = " + hasSound())
            if (hasSound()) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.isEasyLevel = " + isEasyLevel())
            if (isEasyLevel()) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.hasNext = " + hasNext())
            if (hasNext()) foStream.write(1) else foStream.write(0)
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            for ((_, value) in cbGridData.getNextCellIndices()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            var sz = cbGridData.getNextCellIndices().size
            for (i in sz until Constants.NUM_BALLS_USED_DIFF) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            Log.d(TAG, "startSavingGame.getNextCellIndices.size() = $sz")
            foStream.write(sz)
            for ((key) in cbGridData.getNextCellIndices()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().x = " + key.x)
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            Log.d(
                TAG,"startSavingGame.getUndoNextCellIndices().size() = "
                    + cbGridData.getUndoNextCellIndices().size)
            foStream.write(cbGridData.getUndoNextCellIndices().size)
            for ((key) in cbGridData.getUndoNextCellIndices()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().x = " + key.x)
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            // save values on 9x9 grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.ROW_COUNTS) {
                    Log.d(
                        TAG,"startSavingGame.gridData.getCellValue(i, j) = "
                            + cbGridData.getCellValue(i, j))
                    foStream.write(cbGridData.getCellValue(i, j))
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(cbGameProp.currentScore).array()
            Log.d(TAG, "startSavingGame.scoreByte = $scoreByte")
            foStream.write(scoreByte)
            // save undoEnable
            Log.d(TAG, "startSavingGame.isUndoEnable = " + cbGameProp.undoEnable)
            // can undo or no undo
            if (cbGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            // save undoNextBalls
            for ((_, value) in cbGridData.getUndoNextCellIndices()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            sz = cbGridData.getUndoNextCellIndices().size
            for (i in sz until Constants.NUM_BALLS_USED_DIFF) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            // save backupCells
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.ROW_COUNTS) {
                    Log.d(
                        TAG,"startSavingGame.gridData.getBackupCells()[i][j] = "
                            + cbGridData.getBackupCells()[i][j])
                    foStream.write(cbGridData.getBackupCells()[i][j])
                }
            }
            val undoScoreByte = ByteBuffer.allocate(4).putInt(cbGameProp.undoScore).array()
            Log.d(TAG, "startSavingGame.undoScoreByte = $undoScoreByte")
            foStream.write(undoScoreByte)
            foStream.close()
            // end of writing
            Log.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            Log.d(TAG, "startSavingGame.Failed.")
        }
        setScreenMessage("")
        Log.d(TAG, "startSavingGame.Finished")
        cbGameProp.isProcessingJob = false

        return succeeded
    }

    override fun startLoadingGame(): Boolean {
        Log.d(TAG, "startLoadingGame")
        cbGameProp.isProcessingJob = true
        setScreenMessage(loadingGameStr)

        var succeeded = true
        val hasSound: Boolean
        val isEasyLevel: Boolean
        val hasNext: Boolean
        var ballNumOneTime: Int
        val nextBalls = IntArray(Constants.NUM_BALLS_USED_DIFF)
        val gameCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.ROW_COUNTS) }
        val cScore: Int
        val isUndoEnable: Boolean
        val undoNextBalls = IntArray(Constants.NUM_BALLS_USED_DIFF)
        val backupCells = Array(Constants.ROW_COUNTS) {
            IntArray(Constants.ROW_COUNTS) }
        var unScore = cbGameProp.undoScore
        try {
            // clear nextCellIndices and undoNextCellIndices
            cbGridData.setNextCellIndices(HashMap())
            cbGridData.setUndoNextCellIndices(HashMap())
            Log.d(TAG, "startLoadingGame.Creating inputFile")
            val fileName = Utils.getSaveFileName(getWhichGame())
            // File inputFile = new File(mContext.getFilesDir(), savedGameFileName);
            // long fileSizeInByte = inputFile.length();
            // Log.d(TAG, "startLoadingGame.File size = " + fileSizeInByte);
            // FileInputStream fiStream = new FileInputStream(inputFile);
            val fiStream = cbPresenter.fileInputStream(fileName)
            Log.d(TAG, "startLoadingGame.available() = " + fiStream.available())
            Log.d(TAG, "startLoadingGame.getChannel().size() = " + fiStream.channel.size())
            // game sound
            var bValue = fiStream.read()
            hasSound = bValue == 1
            Log.d(TAG, "startLoadingGame.hasSound = $hasSound")
            // game level
            bValue = fiStream.read()
            isEasyLevel = bValue == 1
            Log.d(TAG, "startLoadingGame.isEasyLevel = $isEasyLevel")
            // next balls
            bValue = fiStream.read()
            hasNext = bValue == 1
            Log.d(TAG, "startLoadingGame.hasNextBall = $hasNext")
            ballNumOneTime = fiStream.read()
            Log.i(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
            for (i in 0 until Constants.NUM_BALLS_USED_DIFF) {
                nextBalls[i] = fiStream.read()
                Log.d(TAG, "startLoadingGame.nextCellIndices.cell.getColor() = " + nextBalls[i])
            }
            val nextCellIndicesSize = fiStream.read()
            Log.d(TAG, "startLoadingGame.getNextCellIndices.size() = $nextCellIndicesSize")
            for (i in 0 until nextCellIndicesSize) {
                val x = fiStream.read()
                val y = fiStream.read()
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().x = $x")
                Log.d(TAG, "startLoadingGame.nextCellIndices.getKey().y = $y")
                cbGridData.addNextCellIndices(Point(x, y))
            }
            val undoNextCellIndicesSize = fiStream.read()
            Log.d(
                TAG,"startLoadingGame.getUndoNextCellIndices.size() = " +
                    "$undoNextCellIndicesSize")
            for (i in 0 until undoNextCellIndicesSize) {
                val x = fiStream.read()
                val y = fiStream.read()
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = $x")
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = $y")
                cbGridData.addUndoNextCellIndices(Point(x, y))
            }
            // load values on 9x9 grid
            for (i in 0 until Constants.ROW_COUNTS) {
                for (j in 0 until Constants.ROW_COUNTS) {
                    gameCells[i][j] = fiStream.read()
                    Log.d(TAG, "startLoadingGame.gridData.getCellValue(i, j) = " + gameCells[i][j])
                }
            }
            // reading current score
            val scoreByte = ByteArray(4)
            fiStream.read(scoreByte)
            Log.d(TAG, "startLoadingGame.scoreByte = $scoreByte")
            cScore = ByteBuffer.wrap(scoreByte).getInt()
            // reading undoEnable
            bValue = fiStream.read()
            isUndoEnable = bValue == 1
            Log.d(TAG, "startLoadingGame.isUndoEnable = $isUndoEnable")
            if (isUndoEnable) {
                ballNumOneTime = fiStream.read()
                Log.d(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
                for (i in 0 until Constants.NUM_BALLS_USED_DIFF) {
                    undoNextBalls[i] = fiStream.read()
                    Log.d(
                        TAG,"startLoadingGame.undoNextCellIndices.getValue() = "
                            + undoNextBalls[i])
                }
                // save backupCells
                for (i in 0 until Constants.ROW_COUNTS) {
                    for (j in 0 until Constants.ROW_COUNTS) {
                        backupCells[i][j] = fiStream.read()
                        Log.d(
                            TAG,"startLoadingGame.gridData.getBackupCells()[i][j] = "
                                + backupCells[i][j])
                    }
                }
                val undoScoreByte = ByteArray(4)
                fiStream.read(undoScoreByte)
                Log.d(TAG, "startLoadingGame.undoScoreByte = $undoScoreByte")
                unScore = ByteBuffer.wrap(undoScoreByte).getInt()
            }
            fiStream.close()
            // refresh Main UI with loaded data
            setHasSound(hasSound)
            setEasyLevel(isEasyLevel)
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
            Log.d(TAG, "startLoadingGame.starting displayGameView().")
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

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }
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
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2
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
        if (!isEasyLevel()) {
            // difficult level
            totalScore *= 2 // double of easy level
        }

        return totalScore
    }

    private fun drawBouncyBall(i: Int, j: Int) {
        val color = cbGridData.getCellValue(i, j)
        Log.d(TAG, "drawBouncyBall.($i, $j), color = $color")
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
        Log.d(TAG, "drawNextBall.($i, $j), color = $color")
        val trueColor = if (hasNext()) color else 0
        Log.d(TAG, "drawNextBall.($i, $j), trueColor = $trueColor")
        gridDataArray[i][j].value = ColorBallInfo(trueColor, WhichBall.NEXT_BALL)
    }

    private fun displayNextBallsView() {
        // display the view of next balls
        Log.d(TAG, "displayNextBallsView")
        try {
            for ((key, value) in cbGridData.getNextCellIndices()) {
                Log.d(TAG, "displayNextBallsView.color = $value")
                drawNextBall(key.x, key.y, value)
            }
        } catch (ex: Exception) {
            Log.d(TAG, "displayNextBallsView.Exception: ")
            ex.printStackTrace()
        }
    }

    private fun displayNextColorBalls() {
        if (cbGridData.randThreeCells() == 0) {
            // no vacant, so game over
            gameOver()
            return
        }
        //   display the balls on the nextBallsView
        displayNextBallsView()
    }

    private fun displayGridDataNextCells() {
        Log.d(TAG, "displayGridDataNextCells")
        var n1: Int
        var n2: Int
        var hasMoreFive = false
        val linkedPoint = HashSet<Point>()
        for ((key, value) in cbGridData.getNextCellIndices()) {
            n1 = key.x
            n2 = key.y
            Log.d(TAG, "displayGridDataNextCells.($n1, $n2), color = $value")
            cbGridData.setCellValue(n1, n2, value)
            drawBall(n1, n2, cbGridData.getCellValue(n1, n2))
            if (cbGridData.checkMoreThanFive(n1, n2)) {
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
                cbGridData.getLightLine(), cbGameProp.lastGotScore,
                true, object : ShowScoreCallback {
                    override fun sCallback() {
                        Log.d(TAG, "ShowScoreCallback.sCallback.Do nothing.")
                    }
                })
            Log.d(TAG, "displayGridDataNextCells.post(showScore)")
            showingScoreHandler.post(showScore)
        } else {
            displayNextColorBalls()
        }
    }

    private fun displayGameView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView")
        displayGameGridView()
        // display the view of next balls
        displayNextBallsView()
    }

    private fun drawBallAlongPath() {
        val sizeOfPath = cbGridData.mPathPoint.size
        if (sizeOfPath == 0) {
            Log.w(TAG, "drawBallAlongPath.sizeOfPathPoint == 0")
            return
        }
        cbGameProp.isBallMoving = true
        cbGameProp.isProcessingJob = true

        val beginI = cbGridData.mPathPoint[sizeOfPath - 1].x
        val beginJ = cbGridData.mPathPoint[sizeOfPath - 1].y
        Log.d(TAG, "drawBallAlongPath.beginI = $beginI, beginJ = $beginJ")
        val targetI = cbGridData.mPathPoint[0].x // the target point
        val targetJ = cbGridData.mPathPoint[0].y // the target point
        Log.d(TAG, "drawBallAlongPath.targetI = $targetI, targetJ = $targetJ")
        val color = cbGridData.getCellValue(beginI, beginJ)
        Log.d(TAG, "drawBallAlongPath.color = $color")

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
                    if (cbGridData.checkMoreThanFive(targetI, targetJ)) {
                        cbGameProp.lastGotScore = calculateScore(cbGridData.getLightLine())
                        cbGameProp.undoScore = cbGameProp.currentScore
                        cbGameProp.currentScore += cbGameProp.lastGotScore
                        setCurrentScore(cbGameProp.currentScore)
                        Log.d(TAG, "drawBallAlongPath.showScore")
                        val showScore = ShowScore(
                            cbGridData.getLightLine(), cbGameProp.lastGotScore,
                            false, object : ShowScoreCallback {
                                override fun sCallback() {
                                    Log.d(TAG, "drawBallAlongPath.ShowScoreCallback.sCallback")
                                    cbGameProp.isBallMoving = false
                                    cbGameProp.isProcessingJob = false
                                }
                            })
                        Log.d(TAG, "drawBallAlongPath.showingScoreHandler.post")
                        showingScoreHandler.post(showScore)
                    } else {
                        cbGridData.regenerateNextCellIndices(Point(targetI, targetJ))
                        Log.d(TAG, "drawBallAlongPath.run().displayGridDataNextCells")
                        displayGridDataNextCells() // has a problem
                        Log.d(TAG, "drawBallAlongPath.run() finished.")
                        cbGameProp.isBallMoving = false
                        cbGameProp.isProcessingJob = false
                    }
                }
            }
        }
        Log.d(TAG, "drawBallAlongPath.movingBallHandler.post")
        movingBallHandler.post(runnablePath)
    }

    private inner class ShowScore(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int, val isNextBalls: Boolean,
        val callback: ShowScoreCallback
    ): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            Log.d(TAG, "ShowScore")
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
                2 -> {}
                3 -> {
                    setScreenMessage(lastGotScore.toString())
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, cbGridData.getCellValue(item.x, item.y))
                    }
                    if (isNextBalls) {
                        Log.d(TAG, "ShowScore.onProgressUpdate.displayNextColorBalls")
                        displayNextColorBalls()
                    } else {
                        Log.d(TAG, "ShowScore.onProgressUpdate.displayNextBallsView")
                        displayNextBallsView()
                    }
                }
                4 -> {
                    Log.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
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
            Log.d(TAG, "ShowScore.run().mCounter = $mCounter")
            showingScoreHandler.removeCallbacksAndMessages(null)
            if (mCounter <= twinkleCountDown) {
                val md = mCounter % 2 // modulus
                onProgressUpdate(md)
                showingScoreHandler.postDelayed(this, 100)
            } else {
                if (mCounter == twinkleCountDown + 1) {
                    onProgressUpdate(3) // show score
                    showingScoreHandler.postDelayed(this, 500)
                } else {
                    onProgressUpdate(4) // dismiss showing message
                    callback.sCallback()
                }
            }
        }
    }

    companion object {
        private const val TAG = "CBallViewModel"
    }
}