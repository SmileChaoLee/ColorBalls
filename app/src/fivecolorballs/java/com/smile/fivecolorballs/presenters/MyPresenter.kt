package com.smile.fivecolorballs.presenters

import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.tools.LogUtil
import com.smile.fivecolorballs.constants.FiveBallsConstants
import com.smile.fivecolorballs.models.MyGameProp
import com.smile.fivecolorballs.models.MyGridData
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.utilities.SoundPoolUtil
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.MutableList

class MyPresenter(private val mPresentView: MyPresentView) {

    companion object {
        const val NB_IMAGEVIEW_START_ID: Int = 100
        private const val TAG = "MyPresenter"
        private const val GAME_PROP_TAG = "GamePropState"
        private const val GRID_DATA_TAG = "GridDataState"
        private const val SAVED_GAME_FILENAME = "saved_game"
    }

    private val mSoundPool: SoundPoolUtil = mPresentView.soundPool()
    private val mBouncyHandler = Handler(Looper.getMainLooper())
    private val mMovingBallHandler = Handler(Looper.getMainLooper())
    private val mShowingScoreHandler = Handler(Looper.getMainLooper())

    private var mRowCounts = 0
    private var mColCounts = 0
    private lateinit var mGameProp: MyGameProp
    private lateinit var mGridData: MyGridData

    interface MyPresentView {
        fun getLoadingStr(): String
        fun geSavingGameStr(): String
        fun getLoadingGameStr(): String
        fun getSureToSaveGameStr(): String
        fun getSureToLoadGameStr(): String
        fun getSaveScoreStr(): String
        fun soundPool(): SoundPoolUtil
        fun getScoreDatabase(): ScoreSQLite
        fun fileInputStream(fileName: String): FileInputStream?
        fun fileOutputStream(fileName: String): FileOutputStream?
        fun getColorBallMap(): HashMap<Int, Bitmap>
        fun getColorOvalBallMap(): HashMap<Int, Bitmap>

        fun getImageViewById(id: Int): ImageView
        fun updateHighestScoreOnUi(highestScore: Int)
        fun updateCurrentScoreOnUi(score: Int)
        fun showMessageOnScreen(message: String)
        fun dismissShowMessageOnScreen()
        fun showSaveScoreAlertDialog(entryPoint: Int)
        fun showSaveGameDialog()
        fun showLoadGameDialog()
        fun showGameOverDialog()
    }

    internal fun interface ShowScoreCallback {
        fun sCallback()
    }

    val isProcessingJob: Boolean
        // new added methods
        get() = mGameProp.isProcessingJob

    fun hasNext(): Boolean {
        return mGameProp.hasNext
    }

    fun setHasNext(hasNext: Boolean) {
        mGameProp.hasNext = hasNext
    }

    fun setHasNext(hasNext: Boolean, isNextBalls: Boolean) {
        setHasNext(hasNext)
        if (isNextBalls) {
            displayNextBallsView()
        }
    }

    fun doDrawBallsAndCheckListener(v: View) {
        val i: Int
        val j: Int
        val id: Int = v.id
        i = id / mRowCounts
        j = id % mRowCounts
        if (!mGameProp.isBallBouncing) {
            if (mGridData.getCellValue(i, j) != 0) {
                if ((mGameProp.bouncyBallIndexI == -1) && (mGameProp.bouncyBallIndexJ == -1)) {
                    mGameProp.isBallBouncing = true
                    drawBouncyBall(v as ImageView, mGridData.getCellValue(i, j))
                    mGameProp.bouncyBallIndexI = i
                    mGameProp.bouncyBallIndexJ = j
                }
            }
        } else {
            // cancel the timer
            if (mGridData.getCellValue(i, j) == 0) {
                //   blank cell
                val bouncyBallIndexI = mGameProp.bouncyBallIndexI
                val bouncyBallIndexJ = mGameProp.bouncyBallIndexJ
                if ((bouncyBallIndexI >= 0) && (bouncyBallIndexJ >= 0)) {
                    if (mGridData.canMoveCellToCell(
                            Point(bouncyBallIndexI, bouncyBallIndexJ),
                            Point(i, j)
                        )
                    ) {
                        // cancel the timer
                        mGameProp.isBallBouncing = false
                        cancelBouncyTimer()
                        mGameProp.bouncyBallIndexI = -1
                        mGameProp.bouncyBallIndexJ = -1
                        drawBallAlongPath()
                        mGameProp.isUndoEnable = true
                    } else {
                        //    make a sound
                        if (mGameProp.hasSound) {
                            mSoundPool.playSound()
                        }
                    }
                }
            } else {
                //  cell is not blank
                val bBallIndexI = mGameProp.bouncyBallIndexI
                val bBallIndexJ = mGameProp.bouncyBallIndexJ
                if ((bBallIndexI >= 0) && (bBallIndexJ >= 0)) {
                    val imageView =
                    mPresentView.getImageViewById(bBallIndexI * mRowCounts + bBallIndexJ)
                    cancelBouncyTimer()
                    drawBall(
                        imageView,
                        mGridData.getCellValue(bBallIndexI, bBallIndexJ)
                    )
                    drawBouncyBall(v as ImageView, mGridData.getCellValue(i, j))
                    mGameProp.bouncyBallIndexI = i
                    mGameProp.bouncyBallIndexJ = j
                }
            }
        }
    }

    fun initializeColorBallsGame(
        rowCounts: Int,
        colCounts: Int,
        savedInstanceState: Bundle?
    ): Boolean {
        mRowCounts = rowCounts
        mColCounts = colCounts
        val scoreDb = mPresentView.getScoreDatabase()
        val highestScore = scoreDb.readHighestScore()
        val isNewGame: Boolean
        if (savedInstanceState == null) {
            // activity just started so new game
            LogUtil.d(TAG, "Created new game.")
            isNewGame = true
            mGridData = MyGridData(mRowCounts, mColCounts, Constants.NUM_BALLS_USED_EASY)
            mGameProp = MyGameProp()
        } else {
            LogUtil.d(TAG, "Configuration changed and restore the original UI.")
            val gameProp = savedInstanceState.getParcelable<MyGameProp?>(GAME_PROP_TAG)
            val gridData = savedInstanceState.getParcelable<MyGridData?>(GRID_DATA_TAG)
            if (gameProp == null || gridData == null) {
                isNewGame = true
                mGridData = MyGridData(mRowCounts, mColCounts, Constants.NUM_BALLS_USED_EASY)
                mGameProp = MyGameProp()
            } else {
                isNewGame = false
                mGridData = gridData
                mGameProp = gameProp
            }
        }

        mPresentView.updateHighestScoreOnUi(highestScore)
        mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)

        displayGameView()
        if (isNewGame) {
            displayGridDataNextCells()
        }

        return isNewGame
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(GAME_PROP_TAG, mGameProp)
        outState.putParcelable(GRID_DATA_TAG, mGridData)
    }

    fun isEasyLevel(): Boolean {
        return mGameProp.isEasyLevel
    }

    fun setEasyLevel(yn: Boolean) {
        mGameProp.isEasyLevel = yn
        if (mGameProp.isEasyLevel) {
            // easy level
            mGridData.setNumOfColorsUsed(Constants.NUM_BALLS_USED_EASY)
        } else {
            // difficult
            mGridData.setNumOfColorsUsed(Constants.NUM_BALLS_USED_DIFF)
        }
    }

    fun hasSound(): Boolean {
        return mGameProp.hasSound
    }

    fun setHasSound(hasSound: Boolean) {
        mGameProp.hasSound = hasSound
    }

    fun setShowingNewGameDialog(showingNewGameDialog: Boolean) {
        mGameProp.isShowingNewGameDialog = showingNewGameDialog
    }

    fun setShowingQuitGameDialog(showingQuitGameDialog: Boolean) {
        mGameProp.isShowingQuitGameDialog = showingQuitGameDialog
    }

    fun undoTheLast() {
        if (!mGameProp.isUndoEnable) {
            return
        }
        mGameProp.isProcessingJob = true // started undoing
        mGridData.undoTheLast()
        cancelBouncyTimer()
        mGameProp.isBallBouncing = false
        mGameProp.bouncyBallIndexI = -1
        mGameProp.bouncyBallIndexJ = -1
        // restore the screen
        displayGameView()
        mGameProp.currentScore = mGameProp.undoScore
        mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
        mGameProp.isUndoEnable = false
        mGameProp.isProcessingJob = false // finished
    }

    fun setSaveScoreAlertDialogState(entryPoint: Int, state: Boolean) {
        if (entryPoint == 1) {
            // new game
            setShowingNewGameDialog(state)
        } else {
            // quit game
            setShowingQuitGameDialog(state)
        }
    }

    fun setShowingSureSaveDialog(isShowingSureSaveDialog: Boolean) {
        mGameProp.isShowingSureSaveDialog = isShowingSureSaveDialog
    }

    fun setShowingSureLoadDialog(isShowingSureLoadDialog: Boolean) {
        mGameProp.isShowingSureLoadDialog = isShowingSureLoadDialog
    }

    fun setShowingGameOverDialog(isShowingGameOverDialog: Boolean) {
        mGameProp.isShowingGameOverDialog = isShowingGameOverDialog
    }

    fun setShowingWarningSaveGameDialog(isShowingWarningSaveGameDialog: Boolean) {
        mGameProp.isShowingWarningSaveGameDialog = isShowingWarningSaveGameDialog
    }

    fun saveScore(playerName: String?) {
        // removed on 2019-02-20 no global ranking any more
        // use thread to add a record to database (remote database)
        val score = mGameProp.currentScore
        val restThread: Thread = object : Thread() {
            override fun run() {
                try {
                    // ASP.NET Cor
                    val jsonObject = JSONObject()
                    jsonObject.put("PlayerName", playerName)
                    jsonObject.put("Score", score)
                    jsonObject.put("GameId", Constants.FIVE_COLOR_BALLS_ID)
                    PlayerRecordRest.addOneRecord(jsonObject)
                } catch (ex: Exception) {
                    LogUtil.e(TAG, "saveScore.Exception: ", ex)
                }
            }
        }
        restThread.start()

        val scoreDb = mPresentView.getScoreDatabase()
        val isInTop10 = scoreDb.isInTop10(score)
        if (isInTop10) {
            // inside top 10
            // record the current score
            scoreDb.addScore(playerName, score)
            scoreDb.deleteAllAfterTop10() // only keep the top 10
        }
    }

    fun newGame() {
        mPresentView.showSaveScoreAlertDialog(1)
    }

    fun quitGame() {
        mPresentView.showSaveScoreAlertDialog(0)
    }

    fun saveGame() {
        mPresentView.showSaveGameDialog()
    }

    fun loadGame() {
        mPresentView.showLoadGameDialog()
    }

    fun startSavingGame(): Boolean {
        LogUtil.d(TAG, "startSavingGame")
        mGameProp.isProcessingJob = true
        mPresentView.showMessageOnScreen(mPresentView.geSavingGameStr())
        var succeeded = false
        try {
            val foStream = mPresentView.fileOutputStream(SAVED_GAME_FILENAME)
            if (foStream != null) {
                // save settings
                if (mGameProp.hasSound) {
                    foStream.write(1)
                } else {
                    foStream.write(0)
                }
                if (mGameProp.isEasyLevel) {
                    foStream.write(1)
                } else {
                    foStream.write(0)
                }
                // save next balls
                foStream.write(FiveBallsConstants.BALL_NUM_ONE_TIME)
                for (i in 0..<Constants.NUM_BALLS_USED_DIFF) {
                    foStream.write(mGridData.nextBalls[i])
                }
                // save values on 9x9 grid
                for (i in 0..<mRowCounts) {
                    for (j in 0..<mColCounts) {
                        foStream.write(mGridData.getCellValue(i, j))
                    }
                }
                // save current score
                val scoreByte = ByteBuffer.allocate(4).putInt(mGameProp.currentScore).array()
                foStream.write(scoreByte)
                // save undoEnable
                if (mGameProp.isUndoEnable) {
                    // can undo
                    LogUtil.d(TAG, "startSavingGame.can undo")
                    foStream.write(1)
                    foStream.write(FiveBallsConstants.BALL_NUM_ONE_TIME)
                    // save undoNextBalls
                    for (i in 0..<Constants.NUM_BALLS_USED_DIFF) {
                        foStream.write(mGridData.undoNextBalls[i])
                    }
                    // save backupCells
                    for (i in 0..<mRowCounts) {
                        for (j in 0..<mColCounts) {
                            foStream.write(mGridData.backupCells[i][j])
                        }
                    }
                    val undoScoreByte =
                        ByteBuffer.allocate(4).putInt(mGameProp.undoScore).array()
                    foStream.write(undoScoreByte)
                } else {
                    LogUtil.d(TAG, "startSavingGame.no undo")
                    // no undo
                    foStream.write(0)
                }
                // if showing next balls
                if (mGameProp.hasNext) {
                    foStream.write(1)
                } else {
                    foStream.write(0)
                }
                // end of writing
                foStream.close()
                succeeded = true
                LogUtil.d(TAG, "startSavingGame.Succeeded")
            }
        } catch (ex: IOException) {
            LogUtil.e(TAG, "startSavingGame.IOException: ", ex)
        }

        mGameProp.isProcessingJob = false
        mPresentView.dismissShowMessageOnScreen()

        LogUtil.d(TAG, "startSavingGame() finished")

        return succeeded
    }

    fun startLoadingGame(): Boolean {
        LogUtil.d(TAG, "startLoadingGame")
        mGameProp.isProcessingJob = true
        mPresentView.showMessageOnScreen(mPresentView.getLoadingGameStr())

        var soundYn = mGameProp.hasSound
        var easyYn = mGameProp.isEasyLevel
        var nextYn = mGameProp.hasNext
        val ballNumOneTime: Int
        val nextBalls = IntArray(Constants.NUM_BALLS_USED_DIFF)
        val gameCells = Array(mRowCounts) { IntArray(mColCounts) }
        var cScore = mGameProp.currentScore
        var undoYn = mGameProp.isUndoEnable
        val undoNextBalls = IntArray(Constants.NUM_BALLS_USED_DIFF)
        val backupCells = Array(mRowCounts) { IntArray(mColCounts) }
        var unScore = mGameProp.undoScore

        var succeeded = false
        try {
            val fiStream = mPresentView.fileInputStream(SAVED_GAME_FILENAME)
            if (fiStream != null) {
                var bValue = fiStream.read()
                if (bValue == 1) {
                    // has sound
                    LogUtil.d(TAG, "startLoadingGame.Game has sound")
                    soundYn = true
                } else {
                    // has no sound
                    LogUtil.d(TAG, "startLoadingGame.Game has no sound")
                    soundYn = false
                }
                bValue = fiStream.read()
                if (bValue == 1) {
                    // easy level
                    LogUtil.d(TAG, "startLoadingGame.Game is easy level")
                    easyYn = true
                } else {
                    // difficult level
                    LogUtil.d(TAG, "startLoadingGame.Game is difficult level")
                    easyYn = false
                }
                ballNumOneTime = fiStream.read()
                LogUtil.d(TAG, "startLoadingGame.Game has $ballNumOneTime next balls")
                for (i in 0..<Constants.NUM_BALLS_USED_DIFF) {
                    nextBalls[i] = fiStream.read()
                    LogUtil.d(TAG, "startLoadingGame.Next ball value = $nextBalls[i]")
                }
                for (i in 0..<mRowCounts) {
                    for (j in 0..<mColCounts) {
                        gameCells[i][j] = fiStream.read()
                        LogUtil.d(TAG,
                            "startLoadingGame.Value of ball at (" + i + ", " + j + ") = " +
                                    gameCells[i][j]
                        )
                    }
                }
                // reading current score
                val scoreByte = ByteArray(4)
                fiStream.read(scoreByte)
                cScore = ByteBuffer.wrap(scoreByte).getInt()
                LogUtil.d(TAG, "startLoadingGame.Current score = $cScore")
                // reading undoEnable
                bValue = fiStream.read()
                if (bValue == 1) {
                    // has undo data
                    LogUtil.d(TAG, "startLoadingGame.Game has undo data")
                    undoYn = true
                    fiStream.read()
                    for (i in 0..<Constants.NUM_BALLS_USED_DIFF) {
                        undoNextBalls[i] = fiStream.read()
                    }
                    // save backupCells
                    for (i in 0..<mRowCounts) {
                        for (j in 0..<mColCounts) {
                            backupCells[i][j] = fiStream.read()
                        }
                    }
                    val undoScoreByte = ByteArray(4)
                    fiStream.read(undoScoreByte)
                    unScore = ByteBuffer.wrap(undoScoreByte).getInt()
                    LogUtil.d(TAG, "startLoadingGame.undoScore = $unScore")
                } else {
                    // does not has undo data
                    LogUtil.d(TAG, "startLoadingGame.Game does not has undo data")
                    undoYn = false
                }
                bValue = fiStream.read()
                if (bValue == 1) {
                    // showing next balls
                    LogUtil.d(TAG, "startLoadingGame.Game has next balls")
                    nextYn = true
                } else {
                    // no showing next balls
                    LogUtil.d(TAG, "startLoadingGame.Game has no next balls")
                    nextYn = false
                }
                fiStream.close()
                succeeded = true
            }
        } catch (ex: IOException) {
            LogUtil.e(TAG, "startLoadingGame.IOException: ", ex)
        }

        mPresentView.dismissShowMessageOnScreen()

        if (succeeded) {
            // refresh Main UI with loaded data
            setHasSound(soundYn)
            setEasyLevel(easyYn)
            setHasNext(nextYn, false)
            mGridData.nextBalls = nextBalls
            mGridData.cellValues = gameCells
            mGameProp.currentScore = cScore
            mGameProp.isUndoEnable = undoYn
            mGridData.undoNextBalls = undoNextBalls
            mGridData.backupCells = backupCells
            mGameProp.undoScore = unScore
            // start update UI
            mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
            displayGameView()
        }

        mGameProp.isProcessingJob = false

        return succeeded
    }

    fun release() {
        cancelBouncyTimer()
        mShowingScoreHandler.removeCallbacksAndMessages(null)
        mMovingBallHandler.removeCallbacksAndMessages(null)
        mSoundPool.release()
    }

    private fun gameOver() {
        mPresentView.showGameOverDialog()
    }

    private fun calculateScore(linkedLine: HashSet<Point>): Int {
        // 5 balls --> 5
        // 6 balls --> 5 + (6-5)*2
        // 7 balls --> 5 + (6-5)*2 + (7-5)*2
        // 8 balls --> 5 + (6-5)*2 + (7-5)*2 + (8-5)*2
        // n balls --> 5 + (6-5)*2 + (7-5)*2 + ... + (n-5)*2
        val numBalls = intArrayOf(0, 0, 0, 0, 0, 0)
        for (point in linkedLine) {
            when (mGridData.getCellValue(point.x, point.y)) {
                Constants.COLOR_RED -> {
                    numBalls[0]++
                }
                Constants.COLOR_GREEN -> {
                    numBalls[1]++
                }
                Constants.COLOR_BLUE -> {
                    numBalls[2]++
                }
                Constants.COLOR_MAGENTA -> {
                    numBalls[3]++
                }
                Constants.COLOR_YELLOW -> {
                    numBalls[4]++
                }
                Constants.COLOR_CYAN -> {
                    numBalls[5]++
                }
            }
        }

        return getTotalScore(numBalls)
    }

    private fun getTotalScore(numBalls: IntArray): Int {
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
        if (!mGameProp.isEasyLevel) {
            // difficult level
            totalScore *= 2 // double of easy level
        }
        return totalScore
    }

    private fun drawBall(imageView: ImageView, color: Int) {
        imageView.setImageBitmap(mPresentView.getColorBallMap().get(color))
    }

    private fun drawOval(imageView: ImageView, color: Int) {
        imageView.setImageBitmap(mPresentView.getColorOvalBallMap().get(color))
    }

    private fun displayNextBallsView() {
        // display the view of next balls
        var imageView: ImageView?
        val numOneTime = FiveBallsConstants.BALL_NUM_ONE_TIME
        for (i in 0..<numOneTime) {
            imageView = mPresentView.getImageViewById(NB_IMAGEVIEW_START_ID + i)
            if (mGameProp.hasNext) {
                drawBall(imageView, mGridData.nextBalls[i])
            } else {
                // do not show next balls
                imageView.setImageBitmap(null)
            }
        }
    }

    private fun displayNextColorBalls() {
        mGridData.randColors() //   next  balls
        //   display the balls on the nextBallsView
        displayNextBallsView()
    }

    private fun clearCell(i: Int, j: Int) {
        // int id = i * colCounts + j;
        val id = i * mRowCounts + j
        val imageView = mPresentView.getImageViewById(id)
        imageView.setImageBitmap(null)
        mGridData.setCellValue(i, j, 0)
    }

    private fun displayGridDataNextCells() {
        mGridData.randCells()
        var id: Int
        var n1: Int
        var n2: Int
        var imageView: ImageView?
        var hasMoreFive = false
        val linkedPoint = HashSet<Point>()
        for (nextCellIndex in mGridData.nextCellIndex) {
            n1 = nextCellIndex.x
            n2 = nextCellIndex.y
            id = n1 * mRowCounts + n2
            imageView = mPresentView.getImageViewById(id)
            drawBall(imageView, mGridData.getCellValue(n1, n2))
            if (mGridData.check_moreThanFive(n1, n2)) {
                hasMoreFive = true
                for (point in mGridData.light_line) {
                    if (!linkedPoint.contains(point)) {
                        linkedPoint.add(Point(point))
                    }
                }
            }
        }

        if (hasMoreFive) {
            mGridData.light_line = linkedPoint // added on 2020-07-13
            mGameProp.lastGotScore = calculateScore(mGridData.light_line)
            mGameProp.undoScore = mGameProp.currentScore
            mGameProp.currentScore = mGameProp.currentScore + mGameProp.lastGotScore
            mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
            val showScore = ShowScore(
                mGameProp.lastGotScore,
                mGridData.light_line, true
            ) { mGameProp.isProcessingJob = false }
            mGameProp.isProcessingJob = true
            LogUtil.d(TAG, "displayGridDataNextCells.showingScoreHandler.post().")
            mShowingScoreHandler.post(showScore)
        } else {
            // check if game over
            val gameOverYn = mGridData.getGameOver()
            if (gameOverYn) {
                //  game over
                gameOver()
            } else {
                // game has not been over yet
                displayNextColorBalls()
            }
        }
    }

    private fun displayGameGridView() {
        // display the 9 x 9 game view
        var imageView: ImageView?
        for (i in 0..<mRowCounts) {
            for (j in 0..<mColCounts) {
                val id = i * mRowCounts + j
                imageView = mPresentView.getImageViewById(id)
                val color = mGridData.getCellValue(i, j)
                if (color == 0) {
                    imageView.setImageBitmap(null)
                } else {
                    drawBall(imageView, color)
                }
            }
        }
    }

    private fun displayGameView() {
        // display the view of next balls
        displayNextBallsView()
        // display the 9 x 9 game view
        displayGameGridView()
    }

    private fun drawBallAlongPath() {
        if (mGridData.pathPoint.isEmpty()) return
        val sizeOfPathPoint = mGridData.pathPoint.size
        val pPoint = mGridData.pathPoint
        val ii = pPoint[0].x // the target point
        val jj = pPoint[0].y // the target point
        val beginI = pPoint[sizeOfPathPoint - 1].x
        val beginJ = pPoint[sizeOfPathPoint - 1].y
        val color = mGridData.getCellValue(beginI, beginJ)

        mGameProp.isBallMoving = true
        mGameProp.isProcessingJob = true
        clearCell(beginI, beginJ)

        val tempList: MutableList<Point> = ArrayList(mGridData.pathPoint)
        val runnablePath: Runnable = object : Runnable {
            var ballYN: Boolean = true
            lateinit var imageView: ImageView
            var countDown: Int = tempList.size * 2 - 1

            @Synchronized
            override fun run() {
                if (countDown >= 2) {   // eliminate start point
                    val i = countDown / 2
                    imageView = mPresentView.getImageViewById(
                        tempList[i].x * mRowCounts + tempList[i].y
                    )
                    if (ballYN) {
                        drawBall(imageView, color)
                    } else {
                        imageView.setImageBitmap(null)
                    }
                    ballYN = !ballYN
                    countDown--
                    mMovingBallHandler.postDelayed(this, 20)
                    LogUtil.d(TAG, "drawBallAlongPath.ballMovingHandler.postDelayed()")
                } else {
                    val v = mPresentView.getImageViewById(ii * mRowCounts + jj)
                    mGridData.setCellValue(ii, jj, color)
                    drawBall(v, color)
                    //  check if there are more than five balls with same color connected together
                    if (mGridData.check_moreThanFive(ii, jj)) {
                        mGameProp.lastGotScore = calculateScore(mGridData.light_line)
                        mGameProp.undoScore = mGameProp.currentScore
                        mGameProp.currentScore = mGameProp.currentScore + mGameProp.lastGotScore
                        mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
                        val showScore = ShowScore(
                            mGameProp.lastGotScore,
                            mGridData.light_line, false
                        ) {
                            LogUtil.d(TAG, "drawBallAlongPath.ShowScoreCallback.sCallback")
                            mGameProp.isBallMoving = false
                            mGameProp.isProcessingJob = false
                            LogUtil.d(TAG, "drawBallAlongPath.run().finished.")
                        }
                        LogUtil.d(TAG, "drawBallAlongPath.showingScoreHandler.post().")
                        mShowingScoreHandler.post(showScore)
                    } else {
                        LogUtil.d(TAG, "drawBallAlongPath.displayGridDataNextCells().")
                        displayGridDataNextCells() // has a problem
                        mGameProp.isBallMoving = false
                        mGameProp.isProcessingJob = false
                        LogUtil.d(TAG, "drawBallAlongPath.run().finished.")
                    }
                }
            }
        }
        LogUtil.d(TAG, "drawBallAlongPath.ballMovingHandler.post()")
        mMovingBallHandler.post(runnablePath)
    }

    private fun drawBouncyBall(v: ImageView, color: Int) {
        val bouncyRunnable: Runnable = object : Runnable {
            var ballYN: Boolean = false
            override fun run() {
                if (color != 0) {
                    if (ballYN) {
                        drawBall(v, color)
                    } else {
                        drawOval(v, color)
                    }
                    ballYN = !ballYN
                    mBouncyHandler.postDelayed(this, 200)
                } else {
                    v.setImageDrawable(null)
                }
            }
        }
        mBouncyHandler.post(bouncyRunnable)
    }

    private fun cancelBouncyTimer() {
        mBouncyHandler.removeCallbacksAndMessages(null)
        SystemClock.sleep(20)
    }

    private inner class ShowScore(
        private val lastGotScore: Int, linkedPoint: HashSet<Point>,
        private val isNextBalls: Boolean, private val callback: ShowScoreCallback
    ) : Runnable {
        private val color: Int
        private var hasPoint: HashSet<Point>
        private var counter = 0

        init {
            var colorTmp = 0
            hasPoint = HashSet<Point>(linkedPoint)
            val point = hasPoint.iterator().next()
            colorTmp = mGridData.getCellValue(point.x, point.y)
            color = colorTmp
            mGameProp.isShowNextBallsAfterBlinking = isNextBalls
            mGameProp.isShowingScoreMessage = true
        }

        @Synchronized
        fun onProgressUpdate(status: Int) {
            when (status) {
                0 -> for (item in hasPoint) {
                    val v = mPresentView.getImageViewById(item.x * mRowCounts + item.y)
                    drawBall(v, color)
                }

                1 -> for (item in hasPoint) {
                    val v = mPresentView.getImageViewById(item.x * mRowCounts + item.y)
                    drawOval(v, color)
                }

                2 -> {}
                3 -> {
                    //
                    // show the score
                    val scoreString = lastGotScore.toString()
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.showMessageOnScreen")
                    mPresentView.showMessageOnScreen(scoreString)
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.clearCell")
                    for (item in hasPoint) {
                        clearCell(item.x, item.y)
                    }
                    // added on 2019-03-30
                    if (isNextBalls) {
                        LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.displayNextColorBalls")
                        displayNextColorBalls()
                    }
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.setShowingScoreMessage")
                    mGameProp.isShowingScoreMessage = false
                }

                4 -> {
                    LogUtil.d(TAG, "ShowScoreRunnable.onProgressUpdate.dismissShowMessageOnScreen().")
                    mPresentView.dismissShowMessageOnScreen()
                }
            }
        }

        @Synchronized
        override fun run() {
            counter++
            val twinkleCountDown = 5
            if (counter <= twinkleCountDown) {
                val md = counter % 2 // modulus
                onProgressUpdate(md)
                mShowingScoreHandler.postDelayed(this, 100)
            } else {
                if (counter == twinkleCountDown + 1) {
                    onProgressUpdate(3) // show score
                    mShowingScoreHandler.postDelayed(this, 500)
                } else {
                    onProgressUpdate(4) // dismiss showing message
                    callback.sCallback()
                }
            }
        }
    }
}
