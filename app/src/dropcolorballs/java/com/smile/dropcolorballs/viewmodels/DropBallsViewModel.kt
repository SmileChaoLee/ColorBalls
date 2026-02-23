package com.smile.dropcolorballs.viewmodels

import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.models.GameProp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import com.smile.dropcolorballs.constants.DropBallsConstants
import com.smile.dropcolorballs.models.DropCbGridData
import com.smile.dropcolorballs.presenters.DropBallsPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DropBallsViewModel(private val dropPresenter: DropBallsPresenter)
    : BaseViewModel(dropPresenter) {

    companion object {
        private const val TAG = "DropBallsViewModel"
    }

    private val runningBallsHandler = Handler(Looper.getMainLooper())
    private var dropGameProp: GameProp
    private var dropGridData: DropCbGridData
    private val rbLastIndex = DropBallsConstants.NUM_NEXT_BALLS - 1
    private val runningBalls = ArrayList<Int>()
    private var nextRunningRow = 0
    // one second
    private var droppingSpeed = DropBallsConstants.NORMAL_DROPPING_SPEED
    private var isToEnd = false
    var runningCol = DropBallsConstants.COLUMN_COUNTS / 2
    private var isGameJustStarted = true
    private var isFinishRunning = false
    private var isGameOver = false
    private var gameStartTime = System.currentTimeMillis()

    private val _mGameLevel = mutableIntStateOf(Constants.GAME_LEVEL_1)
    val mGameLevel: Int
        get() = _mGameLevel.intValue

    private val _next4Balls = mutableStateListOf<Int>()
    val next4Balls: List<Int>
        get() = _next4Balls

    // runBallsRunnable will be running in the UI (main) thread
    // if it is handled by runningBallsHandler
    private val runBallsRunnable = object : Runnable {
        override fun run() {
            LogUtil.d(TAG, "runBallsRunnable.run()")
            runningBallsHandler.removeCallbacksAndMessages(null)
            if (dropGameProp.isProcessingJob) {
                runningBallsHandler.postDelayed(this, droppingSpeed)
                return
            }
            showRunningBalls(curRow = nextRunningRow)
            // erase after index is rbLastIndex
            if (nextRunningRow > rbLastIndex) {
                drawBall(nextRunningRow - (rbLastIndex + 1), runningCol, 0)
            }
            nextRunningRow++
            reachBottomOrBlock()
            LogUtil.d(TAG, "runBallsRunnable.isFinishRunning = $isFinishRunning")
            if (!isFinishRunning) {
                runningBallsHandler.postDelayed(this, droppingSpeed)
            }
        }
    }

    init {
        LogUtil.i(TAG, "DropBallsViewModel.init")
        dropGameProp = GameProp()
        dropGridData = DropCbGridData()
        mGameProp = dropGameProp
        mGridData = dropGridData
        super.setProperties()
    }

    fun setDropGameLevel(gameLevel: Int) {
        LogUtil.i(TAG, "setDropGameLevel")
        super.setGameLevel(gameLevel, false)
        _mGameLevel.intValue = dropGameProp.gameLevel
    }

    override fun cellClickListener(i: Int, j: Int) {
        LogUtil.i(TAG, "cellClickListener.($i, $j)")
        // do nothing
    }

    private fun setData(prop: GameProp, gData: DropCbGridData) {
        LogUtil.i(TAG, "setData")
        dropGameProp = prop
        dropGridData = gData
        // update mGameProp and mGridData in BaseViewModel
        mGameProp = prop
        mGridData = gData
    }

    private fun initData() {
        LogUtil.i(TAG, "initData")
        dropGameProp.initializeKeepSetting(getWhichGame())
        dropGridData.initialize()
    }

    override fun initGame(bundle: Bundle?) {
        LogUtil.i(TAG, "initGame = $bundle")
        dropGameProp.isProcessingJob = true
        val isNewGame = restoreState(bundle)
        LogUtil.i(TAG, "initGame.isNewGame = $isNewGame")
        _mGameLevel.intValue = dropGameProp.gameLevel
        setCurrentScore(dropGameProp.currentScore)
        displayGameGridView()
        getAndSetHighestScore() // a coroutine operation
        isToEnd = false
        isFinishRunning = false
        isGameOver = false
        setDroppingSpeed(DropBallsConstants.NORMAL_DROPPING_SPEED)
        dropGameProp.isProcessingJob = false
        gameStartTime = System.currentTimeMillis()
        isGameJustStarted = true
        startRunBalls()
    }

    fun setDroppingSpeed(seconds: Long) {
        runningBallsHandler.removeCallbacksAndMessages(null)
        droppingSpeed = if (seconds < DropBallsConstants.MIN_DROPPING_SPEED)
            DropBallsConstants.MIN_DROPPING_SPEED
        else seconds
        runningBallsHandler.post(runBallsRunnable)
    }

    fun toDropToEnd() {
        if (dropGameProp.isProcessingJob) return
        if (isToEnd) return
        isToEnd = true
        runningBallsHandler.removeCallbacksAndMessages(null)
        droppingSpeed = DropBallsConstants.END_DROPPING_SPEED
        runningBallsHandler.post(runBallsRunnable)
    }

    fun startRunBalls(row: Int = 0) {
        LogUtil.i(TAG,"startRunBalls")
        runningBalls.clear()
        runningBalls.addAll(dropGridData.runningBalls)
        if (runningBalls.isEmpty()) {
            LogUtil.i(TAG,"startRunBalls.runningBalls.size = 0")
            return
        }
        _next4Balls.clear()
        _next4Balls.addAll(dropGridData.next4Balls)
        if (next4Balls.isEmpty()) {
            LogUtil.i(TAG,"startRunBalls.next4Balls.size = 0")
            return
        }
        isToEnd = false
        isFinishRunning = false
        nextRunningRow = row
        runningCol = DropBallsConstants.COLUMN_COUNTS / 2
        // one minute speed up 1 ms
        val passTime = (System.currentTimeMillis() - gameStartTime) /
                DropBallsConstants.INCREASE_SPEED_PERIOD
        setDroppingSpeed(DropBallsConstants.NORMAL_DROPPING_SPEED - passTime)
        LogUtil.i(TAG,"startRunBalls.droppingSpeed = $droppingSpeed")
        isGameJustStarted = false
        runningBallsHandler.post(runBallsRunnable)
    }

    private fun eraseRunningBalls(curRow: Int) {
        // erase previous Running Balls
        for (k in 0..minOf(curRow, rbLastIndex)) {
            drawBall(curRow - k, runningCol, 0)
        }
    }

    private fun showRunningBalls(curRow: Int) {
        for (k in 0..minOf(curRow, rbLastIndex)) {
            drawBall(
                curRow - k, runningCol,
                runningBalls[rbLastIndex - k]
            )
        }
    }

    private fun reachBottomOrBlock() {
        LogUtil.d(TAG,"reachBottomOrBlock.nextRunningRow = $nextRunningRow")
        if (nextRunningRow < DropBallsConstants.ROW_COUNTS) {
            val ballColor = dropGridData.mCellValues[nextRunningRow][runningCol]
            LogUtil.d(TAG,"reachBottomOrBlock.ballColor = $ballColor")
            if (ballColor != 0) isFinishRunning = true
        } else {
            isFinishRunning = true
        }
        LogUtil.d(TAG,"reachBottomOrBlock.isFinishRunning = $isFinishRunning")
        if (isFinishRunning) {
            runningBallsHandler.removeCallbacksAndMessages(null)
            val threeSet = HashSet<Point>()
            val curRow = if (nextRunningRow == 0) 0 else nextRunningRow - 1
            val minIndex = minOf(curRow, rbLastIndex)
            for (k in 0..minOf(curRow, minIndex)) {
                // update dropGridData.mCellValues[][]
                dropGridData.mCellValues[curRow - k][runningCol] = runningBalls[rbLastIndex - k]
                threeSet.add(Point(curRow-k,runningCol))
            }
            val moreThanNum = dropGridData.moreThanNum(mGameLevel, threeSet)
            if (!moreThanNum) {
                dropGridData.setNextRunning()
                if (nextRunningRow < DropBallsConstants.NUM_NEXT_BALLS) {
                    // Game over
                    isGameOver = true
                    gameOver()
                } else {
                    startRunBalls()
                }
                return
            }
            startCrashBalls()
        }
    }

    private fun startCrashBalls() {
        LogUtil.d(TAG, "startCrashBalls")
        dropGameProp.isProcessingJob = true
        val tempLine = HashSet(dropGridData.addUpLightLine)
        LogUtil.d(TAG, "startCrashBalls.tempLine.size = ${tempLine.size}")
        dropGameProp.lastGotScore = calculateScore(tempLine)
        dropGameProp.currentScore += dropGameProp.lastGotScore
        setCurrentScore(dropGameProp.currentScore)
        val showScore = ShowScore(
            dropGridData,
            tempLine,
            dropGameProp.lastGotScore,
            false /* no used*/,
            object : ShowScoreCallback {
                override fun sCallback() {
                    LogUtil.d(TAG, "startCrashBalls.sCallback")
                    viewModelScope.launch(Dispatchers.Default) {
                        // Refresh the game view
                        val canCrashAgain = dropGridData.canCrashAgain(mGameLevel)
                        LogUtil.d(TAG, "startCrashBalls.sCallback.canCrashAgain = $canCrashAgain")
                        displayGameGridView()
                        if (canCrashAgain) {
                            startCrashBalls()    // recursion
                        } else {
                            // check if game over
                            dropGridData.setNextRunning()
                            dropGameProp.isProcessingJob = false
                            startRunBalls()
                        }
                    }
                }
            })
        showingScoreHandler.post(showScore)
    }

    fun shiftRunningCol(addValue: Int) {
        LogUtil.d(TAG,"shiftRunningCol")
        if (dropGameProp.isProcessingJob) return
        if (isFinishRunning) return
        if (runningCol + addValue < 0) return
        if (runningCol + addValue >= DropBallsConstants.COLUMN_COUNTS) return

        // no need because this method is running in the same thread as
        // runBallsRunnable is running
        // runningBallsHandler.removeCallbacksAndMessages(null)
        val curRow = if (nextRunningRow == 0) 0 else nextRunningRow - 1
        var isBlocked = false
        for (k in 0 .. minOf(curRow, rbLastIndex)) {
            if (dropGridData.mCellValues[curRow - k][runningCol+addValue] != 0) {
                isBlocked = true
                break
            }
        }
        if (!isBlocked) {
            eraseRunningBalls(curRow = curRow)
            // change column
            runningCol += addValue
            showRunningBalls(curRow = curRow)
            // check if it meet the bottom or having a ball under
            reachBottomOrBlock()
        }
        // runningBallsHandler.post(runBallsRunnable)
        // startRunBalls(row = runningRow)
    }

    fun rotateRunningBalls() {
        LogUtil.i(TAG,"rotateRunningBalls")
        if (dropGameProp.isProcessingJob) return
        val first = runningBalls[0]
        for (i in 0 until rbLastIndex) {
            runningBalls[i] = runningBalls[i+1]
        }
        runningBalls[rbLastIndex] = first
        val curRow = if (nextRunningRow == 0) 0 else nextRunningRow - 1
        showRunningBalls(curRow = curRow )
    }

    fun startRunningHandler() {
        LogUtil.i(TAG,"startRunningHandler")
        if (isGameJustStarted) {
            LogUtil.i(TAG,"startRunningHandler.no start, return")
            return
        }
        runningBallsHandler.post(runBallsRunnable)
    }

    fun stopRunningHandler() {
        LogUtil.i(TAG,"stopRunningHandler")
        runningBallsHandler.removeCallbacksAndMessages(null)
    }

    private fun restoreState(state: Bundle?): Boolean {
        LogUtil.i(TAG,"restoreState.state = $state")
        var isNewGame = true
        var gameProp: GameProp? = null
        var gridData: DropCbGridData? = null
        state?.let {
            LogUtil.d(TAG,"restoreState.state not null")
            gameProp =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.getParcelable(Constants.GAME_PROP_TAG,
                        GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    it.getParcelable(Constants.GRID_DATA_TAG,
                        DropCbGridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
            isNewGame = false
        }
        LogUtil.i(TAG, "restoreState.isNewGame = $isNewGame")
        if (isNewGame) {
            initData()
        } else {
            setData(gameProp!!, gridData!!)
        }

        return isNewGame
    }

    override fun release() {
        super.release()
        runningBallsHandler.removeCallbacksAndMessages(null)
    }

    override fun saveInstanceState(outState: Bundle) {
        runningBallsHandler.removeCallbacksAndMessages(null)
        outState.putParcelable(Constants.GAME_PROP_TAG, dropGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, dropGridData)
    }

    override fun undoTheLast() {
        LogUtil.i(TAG, "undoTheLast.undoEnable")
        // do nothing
    }

    override fun newGame() {
        // creating a new game
        LogUtil.i(TAG, "newGame")
        runningBallsHandler.removeCallbacksAndMessages(null)
        mGameAction = Constants.IS_CREATING_GAME
        setSaveScoreTitle(saveScoreStr)
        isGameJustStarted = true
    }

    override fun startSavingGame(): Boolean {
        LogUtil.i(TAG, "startSavingGame")
        return true
    }

    override fun startLoadingGame(): Boolean {
        LogUtil.i(TAG, "startLoadingGame")
        return true
    }

    override fun dealWithIsNextBalls(isNextBalls: Boolean) {
        LogUtil.i(TAG, "dealWithIsNextBalls")
        // do nothing
    }

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }
        var score = 0
        // level 1
        // one point for each ball
        when (dropGameProp.gameLevel) {
            Constants.GAME_LEVEL_1 -> {
                // level 1
                // one point for each ball
                score = linkedLine.size
            }
            Constants.GAME_LEVEL_2 -> {
                // level 2
                // 1.5 point for each ball and then cut the decimal part
                score = (linkedLine.size * 1.5f).toInt()
            }
            Constants.GAME_LEVEL_3 -> {
                // other levels
            }
            Constants.GAME_LEVEL_4 -> {
                // other levels
            }
            Constants.GAME_LEVEL_5 -> {
                // other levels
            }
        }
        return score
    }
}