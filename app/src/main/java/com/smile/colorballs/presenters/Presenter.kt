package com.smile.colorballs.presenters

import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.os.BundleCompat
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.interfaces.PresentView
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.models.GridData
import com.smile.smilelibraries.player_record_rest.PlayerRecordRest
import com.smile.smilelibraries.utilities.SoundPoolUtil
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer

class Presenter private constructor() {
    private lateinit var mPresentView: PresentView
    private lateinit var soundPool: SoundPoolUtil
    private lateinit var colorBallMap: HashMap<Int, Drawable>
    private lateinit var colorOvalBallMap: HashMap<Int, Drawable>
    private lateinit var colorNextBallMap: HashMap<Int, Drawable>
    private var bouncyAnimation: AnimationDrawable? = null
    lateinit var mGameProp: GameProp
    lateinit var mGridData: GridData
    private val movingBallHandler = Handler(Looper.getMainLooper())
    private val showingScoreHandler = Handler(Looper.getMainLooper())
    private val rowCounts = Constants.ROW_COUNTS
    private val colCounts = Constants.COLUMN_COUNTS

    private interface ShowScoreCallback {
        fun sCallback()
    }

    constructor(presentView: PresentView) : this() {
        mPresentView = presentView
        colorBallMap = HashMap()
        colorOvalBallMap = HashMap()
        colorNextBallMap = HashMap()
        soundPool = mPresentView.soundPool()
    }

    fun drawBallsAndCheckListener(v: View) {
        Log.d(TAG, "drawBallsAndCheckListener")
        val id = v.id
        val i = getRow(id)
        val j = getColumn(id)
        Log.d(TAG, "drawBallsAndCheckListener.isBallBouncing = ${mGameProp.isBallBouncing}")
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
            // cancel the bouncy timer
            val bouncyI = mGameProp.bouncyBallIndexI
            val bouncyJ = mGameProp.bouncyBallIndexJ
            if (mGridData.getCellValue(i, j) == 0) {
                //   blank cell
                if ((bouncyI >= 0) && (bouncyJ >= 0)) {
                    if (mGridData.canMoveCellToCell(Point(bouncyI, bouncyJ), Point(i, j))) {
                        // cancel the timer
                        mGameProp.isBallBouncing = false
                        stopBouncyAnimation()
                        mGameProp.bouncyBallIndexI = -1
                        mGameProp.bouncyBallIndexJ = -1
                        drawBallAlongPath()
                        mGameProp.undoEnable = true
                    } else {
                        //  make a sound
                        if (mGameProp.hasSound) {
                            soundPool.playSound()
                        }
                    }
                }
            } else {
                //  cell is not blank
                if ((bouncyI >= 0) && (bouncyJ >= 0)) {
                    stopBouncyAnimation()
                    val imageView = mPresentView.getImageViewById(getImageId(bouncyI, bouncyJ))
                    drawBall(imageView, mGridData.getCellValue(bouncyI, bouncyJ))
                    drawBouncyBall(v as ImageView, mGridData.getCellValue(i, j))
                    mGameProp.bouncyBallIndexI = i
                    mGameProp.bouncyBallIndexJ = j
                }
            }
        }
    }

    fun initGame(cellWidth: Int, cellHeight: Int, state: Bundle?): Boolean {
        bitmapDrawableResources(cellWidth, cellHeight)
        val highestScore = mPresentView.highestScore()
        val isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: GridData? = null
        state?.let {
            Log.d(TAG,"initGame.state not null then restore the original UI")
            gameProp =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GAME_PROP_TAG, GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GRID_DATA_TAG, GridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
        }
        if (gameProp == null || gridData == null) {
            Log.d(TAG, "initGame.gameProp or gridData is null, new game")
            mGameProp = GameProp()
            mGridData = GridData()
            isNewGame = true
        } else {
            mGameProp = gameProp!!
            mGridData = gridData!!
            isNewGame = false
        }
        ColorBallsApp.isShowingLoadingMessage = mGameProp.isShowingLoadingMessage
        ColorBallsApp.isProcessingJob = mGameProp.isProcessingJob
        mPresentView.updateHighestScoreOnUi(highestScore)
        mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)

        displayGameView()
        if (isNewGame) {    // new game
            displayGridDataNextCells()
        } else {
            // display the original state before changing configuration
            // need to be tested
            if (ColorBallsApp.isShowingLoadingMessage) {
                mPresentView.showLoadingStrOnScreen()
            }
            if (mGameProp.isBallMoving) {
                Log.d(TAG, "initGame.mGameProp.isBallMoving() is true")
                drawBallAlongPath()
            }
            if (mGameProp.isShowingScoreMessage) {
                Log.d(TAG, "initGame.mGameProp.isShowingScoreMessage() is true")
                val showScore = ShowScore(
                    mGridData.getLightLine(), mGameProp.lastGotScore,
                    mGameProp.isShowNextBallsAfterBlinking,
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
        return isNewGame
    }

    private fun lastPartOfInitialGame() {
        if (mGameProp.isBallBouncing) {
            val i = mGameProp.bouncyBallIndexI
            val j = mGameProp.bouncyBallIndexJ
            val v = mPresentView.getImageViewById(getImageId(i, j))
            drawBouncyBall(v, mGridData.getCellValue(i, j))
        }
        if (mGameProp.isShowingNewGameDialog) {
            Log.d(TAG, "lastPartOfInitialGame.newGame()")
            newGame()
        }
        if (mGameProp.isShowingQuitGameDialog) {
            Log.d(TAG, "lastPartOfInitialGame.show quitGame()")
            quitGame()
        }
        if (mGameProp.isShowingSureSaveDialog) {
            Log.d(TAG, "lastPartOfInitialGame.saveGame()")
            saveGame()
        }
        if (mGameProp.isShowingSureLoadDialog) {
            Log.d(TAG, "lastPartOfInitialGame.loadGame()")
            loadGame()
        }
        if (mGameProp.isShowingGameOverDialog) {
            Log.d(TAG, "lastPartOfInitialGame.gameOver()")
            gameOver()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        mGameProp.isShowingLoadingMessage = ColorBallsApp.isShowingLoadingMessage
        mGameProp.isProcessingJob = ColorBallsApp.isProcessingJob
        outState.putParcelable(Constants.GAME_PROP_TAG, mGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, mGridData)
    }

    fun completedAll(): Boolean {
        for (thCompleted in mGameProp.threadCompleted) {
            if (!thCompleted) {
                return false
            }
        }
        return true
    }

    fun hasSound(): Boolean {
        println("Presenter.hasSound.mGameProp = $mGameProp")
        println("Presenter.hasSound.hasSound = ${mGameProp.hasSound}")
        return mGameProp.hasSound
    }

    fun setHasSound(hasSound: Boolean) {
        println("Presenter.setHasSound.mGameProp = $mGameProp")
        println("Presenter.setHasSound.hasSound = $hasSound")
        mGameProp.hasSound = hasSound
    }

    fun isEasyLevel(): Boolean {
        return mGameProp.isEasyLevel
    }

    fun setEasyLevel(yn: Boolean) {
        mGameProp.isEasyLevel = yn
        mGridData.setNumOfColorsUsed(if (yn) Constants.NUM_EASY else Constants.NUM_DIFFICULT)
    }

    fun hasNextBall(): Boolean {
        return mGameProp.hasNextBall
    }

    fun setHasNextBall(hasNextBall: Boolean, isNextBalls: Boolean) {
        mGameProp.hasNextBall = hasNextBall
        if (isNextBalls) {
            displayNextBallsView()
        }
    }

    private fun setShowingNewGameDialog(showingNewGameDialog: Boolean) {
        mGameProp.isShowingNewGameDialog = showingNewGameDialog
    }

    private fun setShowingQuitGameDialog(showingQuitGameDialog: Boolean) {
        mGameProp.isShowingQuitGameDialog = showingQuitGameDialog
    }

    fun undoTheLast() {
        if (!mGameProp.undoEnable) {
            return
        }
        ColorBallsApp.isProcessingJob = true // started undoing
        mGridData.undoTheLast()
        stopBouncyAnimation()
        mGameProp.isBallBouncing = false
        mGameProp.bouncyBallIndexI = -1
        mGameProp.bouncyBallIndexJ = -1
        // restore the screen
        displayGameView()
        mGameProp.currentScore = mGameProp.undoScore
        mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
        // completedPath = true;
        mGameProp.undoEnable = false
        ColorBallsApp.isProcessingJob = false // finished
    }

    fun setSaveScoreAlertDialogState(entryPoint: Int, state: Boolean) {
        ColorBallsApp.isProcessingJob = state
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

    fun saveScore(playerName: String, score: Int) {
        // use thread to add a record to remote database
        val restThread: Thread = object : Thread() {
            override fun run() {
                try {
                    // ASP.NET Core
                    val jsonObject = JSONObject()
                    jsonObject.put("PlayerName", playerName)
                    jsonObject.put("Score", score)
                    jsonObject.put("GameId", Constants.GAME_ID)
                    PlayerRecordRest.addOneRecord(jsonObject)
                    Log.d(TAG, "saveScore.Succeeded to add one record to remote.")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Log.d(TAG, "saveScore.Failed to add one record to remote.")
                }
            }
        }
        restThread.start()

        // save to local storage
        mPresentView.addScoreInLocalTop10(playerName, score)
    }

    fun newGame() {
        mPresentView.showSaveScoreAlertDialog(1, mGameProp.currentScore)
    }

    fun quitGame() {
        mPresentView.showSaveScoreAlertDialog(0, mGameProp.currentScore)
    }

    fun saveGame() {
        mPresentView.showSaveGameDialog()
    }

    fun loadGame() {
        mPresentView.showLoadGameDialog()
    }

    fun readNumberOfSaved(): Int {
        Log.d(TAG, "readNumberOfSaved")
        var numOfSaved = 0
        try {
            val fiStream = mPresentView.fileInputStream(NUM_SAVE_FILENAME)
            numOfSaved = fiStream.read()
            fiStream.close()
        } catch (ex: IOException) {
            Log.d(TAG, "readNumberOfSaved.IOException")
            ex.printStackTrace()
        }
        return numOfSaved
    }

    fun startSavingGame(num: Int): Boolean {
        Log.d(TAG, "startSavingGame")
        ColorBallsApp.isProcessingJob = true
        mPresentView.showSavingGameStrOnScreen()

        var numOfSaved = num
        var succeeded = true
        try {
            var foStream = mPresentView.fileOutputStream(SAVE_FILENAME)
            // save settings
            Log.d(TAG, "startSavingGame.hasSound = " + mGameProp.hasSound)
            if (mGameProp.hasSound) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.isEasyLevel = " + mGameProp.isEasyLevel)
            if (mGameProp.isEasyLevel) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.hasNextBall = " + mGameProp.hasNextBall)
            if (mGameProp.hasNextBall) foStream.write(1) else foStream.write(0)
            // save next balls
            // foStream.write(gridData.ballNumOneTime);
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            for ((_, value) in mGridData.getNextCellIndices()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            var sz = mGridData.getNextCellIndices().size
            for (i in sz until Constants.NUM_DIFFICULT) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            Log.d(TAG, "startSavingGame.getNextCellIndices.size() = $sz")
            foStream.write(sz)
            for ((key) in mGridData.getNextCellIndices()) {
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().x = " + key.x)
                Log.d(TAG, "startSavingGame.nextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            Log.d(TAG,"startSavingGame.getUndoNextCellIndices().size() = "
                    + mGridData.getUndoNextCellIndices().size)
            foStream.write(mGridData.getUndoNextCellIndices().size)
            for ((key) in mGridData.getUndoNextCellIndices()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().x = " + key.x)
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().y = " + key.y)
                foStream.write(key.x)
                foStream.write(key.y)
            }
            // save values on 9x9 grid
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    Log.d(TAG,"startSavingGame.gridData.getCellValue(i, j) = "
                            + mGridData.getCellValue(i, j))
                    foStream.write(mGridData.getCellValue(i, j))
                }
            }
            // save current score
            val scoreByte = ByteBuffer.allocate(4).putInt(mGameProp.currentScore).array()
            Log.d(TAG, "startSavingGame.scoreByte = $scoreByte")
            foStream.write(scoreByte)
            // save undoEnable
            Log.d(TAG, "startSavingGame.isUndoEnable = " + mGameProp.undoEnable)
            // can undo or no undo
            if (mGameProp.undoEnable) foStream.write(1) else foStream.write(0)
            Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
            foStream.write(Constants.BALL_NUM_ONE_TIME)
            // save undoNextBalls
            for ((_, value) in mGridData.getUndoNextCellIndices()) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = $value")
                foStream.write(value)
            }
            sz = mGridData.getUndoNextCellIndices().size
            for (i in sz until Constants.NUM_DIFFICULT) {
                Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + 0)
                foStream.write(0)
            }
            // save backupCells
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    Log.d(TAG,"startSavingGame.gridData.getBackupCells()[i][j] = "
                            + mGridData.getBackupCells()[i][j])
                    foStream.write(mGridData.getBackupCells()[i][j])
                }
            }
            val undoScoreByte = ByteBuffer.allocate(4).putInt(mGameProp.undoScore).array()
            Log.d(TAG, "startSavingGame.undoScoreByte = $undoScoreByte")
            foStream.write(undoScoreByte)
            foStream.close()
            // end of writing
            numOfSaved++
            // save numOfSaved back to file (ColorBallsApp.NumOfSavedGameFileName)
            Log.d(TAG, "startSavingGame.creating fileOutputStream.")
            foStream = mPresentView.fileOutputStream(NUM_SAVE_FILENAME)
            foStream.write(numOfSaved)
            foStream.close()
            Log.d(TAG, "startSavingGame.Succeeded.")
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
            Log.d(TAG, "startSavingGame.Failed.")
        }

        ColorBallsApp.isProcessingJob = false
        mPresentView.dismissShowMessageOnScreen()
        Log.d(TAG, "startSavingGame.Finished")
        return succeeded
    }

    fun startLoadingGame(): Boolean {
        Log.d(TAG, "startLoadingGame")
        ColorBallsApp.isProcessingJob = true
        mPresentView.showLoadingGameStrOnScreen()

        var succeeded = true
        val hasSound: Boolean
        val isEasyLevel: Boolean
        val hasNextBall: Boolean
        var ballNumOneTime: Int
        val nextBalls = IntArray(Constants.NUM_DIFFICULT)
        val gameCells = Array(rowCounts) { IntArray(colCounts) }
        val cScore: Int
        val isUndoEnable: Boolean
        val undoNextBalls = IntArray(Constants.NUM_DIFFICULT)
        val backupCells = Array(rowCounts) { IntArray(colCounts) }
        var unScore = mGameProp.undoScore
        try {
            // clear nextCellIndices and undoNextCellIndices
            mGridData.setNextCellIndices(HashMap())
            mGridData.setUndoNextCellIndices(HashMap())
            Log.d(TAG, "startLoadingGame.Creating inputFile")
            // File inputFile = new File(mContext.getFilesDir(), savedGameFileName);
            // long fileSizeInByte = inputFile.length();
            // Log.d(TAG, "startLoadingGame.File size = " + fileSizeInByte);
            // FileInputStream fiStream = new FileInputStream(inputFile);
            val fiStream = mPresentView.fileInputStream(SAVE_FILENAME)
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
            hasNextBall = bValue == 1
            Log.d(TAG, "startLoadingGame.hasNextBall = $hasNextBall")
            ballNumOneTime = fiStream.read()
            Log.i(TAG, "startLoadingGame.ballNumOneTime = $ballNumOneTime")
            for (i in 0 until Constants.NUM_DIFFICULT) {
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
                mGridData.addNextCellIndices(Point(x, y))
            }
            val undoNextCellIndicesSize = fiStream.read()
            Log.d(TAG,"startLoadingGame.getUndoNextCellIndices.size() = " +
                    "$undoNextCellIndicesSize")
            for (i in 0 until undoNextCellIndicesSize) {
                val x = fiStream.read()
                val y = fiStream.read()
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = $x")
                Log.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = $y")
                mGridData.addUndoNextCellIndices(Point(x, y))
            }
            // load values on 9x9 grid
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
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
                for (i in 0 until Constants.NUM_DIFFICULT) {
                    undoNextBalls[i] = fiStream.read()
                    Log.d(TAG,"startLoadingGame.undoNextCellIndices.getValue() = "
                            + undoNextBalls[i])
                }
                // save backupCells
                for (i in 0 until rowCounts) {
                    for (j in 0 until colCounts) {
                        backupCells[i][j] = fiStream.read()
                        Log.d(TAG,"startLoadingGame.gridData.getBackupCells()[i][j] = "
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
            setHasNextBall(hasNextBall, false)
            var kk = 0
            for (entry in mGridData.getNextCellIndices().entries) {
                entry.setValue(nextBalls[kk++])
            }
            mGridData.setCellValues(gameCells)
            mGameProp.currentScore = cScore
            mGameProp.undoEnable = isUndoEnable
            kk = 0
            for (entry in mGridData.getUndoNextCellIndices().entries) {
                entry.setValue(undoNextBalls[kk++])
            }
            mGridData.setBackupCells(backupCells)
            mGameProp.undoScore = unScore
            // start update UI
            mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
            Log.d(TAG, "startLoadingGame.starting displayGameView().")
            displayGameView()
        } catch (ex: IOException) {
            ex.printStackTrace()
            succeeded = false
        }
        ColorBallsApp.isProcessingJob = false
        mPresentView.dismissShowMessageOnScreen()

        return succeeded
    }

    fun release() {
        stopBouncyAnimation()
        showingScoreHandler.removeCallbacksAndMessages(null)
        movingBallHandler.removeCallbacksAndMessages(null)
        soundPool.release()
    }

    private fun bitmapDrawableResources(cellWidth: Int, cellHeight: Int) {
        Log.w(TAG, "bitmapDrawableResources")
        require(!(cellWidth <= 0 || cellHeight <= 0)) { "cellWidth and cellHeight must be > 0" }

        val resources = mPresentView.contextResources()
        println("bitmapDrawableResources.resources = $resources")
        val nextBallWidth = (cellWidth * 0.5f).toInt()
        val nextBallHeight = (cellHeight * 0.5f).toInt()
        val ovalBallWidth = (cellWidth * 0.9f).toInt()
        val ovalBallHeight = (cellHeight * 0.7f).toInt()

        mPresentView.compatDrawable(R.drawable.redball)?.let { draw ->
            colorBallMap[Constants.COLOR_RED] = draw
        }
        BitmapFactory.decodeResource(resources, R.drawable.redball)?.let { bm ->
            mPresentView.bitmapToDrawable(bm, nextBallWidth, nextBallHeight)?.let { draw ->
                colorNextBallMap[Constants.COLOR_RED] = draw
            }
            mPresentView.bitmapToDrawable(bm, ovalBallWidth, ovalBallHeight)?.let { draw ->
                colorOvalBallMap[Constants.COLOR_RED] = draw
            }
        }

        mPresentView.compatDrawable(R.drawable.greenball)?.let { draw ->
            colorBallMap[Constants.COLOR_GREEN] = draw
        }
        BitmapFactory.decodeResource(resources, R.drawable.greenball)?.let { bm ->
            mPresentView.bitmapToDrawable(bm, nextBallWidth, nextBallHeight)?.let { draw ->
                colorNextBallMap[Constants.COLOR_GREEN] = draw
            }
            mPresentView.bitmapToDrawable(bm, ovalBallWidth, ovalBallHeight)?.let { draw ->
                colorOvalBallMap[Constants.COLOR_GREEN] = draw
            }
        }

        mPresentView.compatDrawable(R.drawable.blueball)?.let { draw ->
            colorBallMap[Constants.COLOR_BLUE] = draw
        }
        BitmapFactory.decodeResource(resources, R.drawable.blueball)?.let { bm ->
            mPresentView.bitmapToDrawable(bm, nextBallWidth, nextBallHeight)?.let { draw ->
                colorNextBallMap[Constants.COLOR_BLUE] = draw
            }
            mPresentView.bitmapToDrawable(bm, ovalBallWidth, ovalBallHeight)?.let { draw ->
                colorOvalBallMap[Constants.COLOR_BLUE] = draw
            }
        }

        mPresentView.compatDrawable(R.drawable.magentaball)?.let { draw ->
            colorBallMap[Constants.COLOR_MAGENTA] = draw
        }
        BitmapFactory.decodeResource(resources, R.drawable.magentaball)?.let { bm ->
            mPresentView.bitmapToDrawable(bm, nextBallWidth, nextBallHeight)?.let { draw ->
                colorNextBallMap[Constants.COLOR_MAGENTA] = draw
            }
            mPresentView.bitmapToDrawable(bm, ovalBallWidth, ovalBallHeight)?.let { draw ->
                colorOvalBallMap[Constants.COLOR_MAGENTA] = draw
            }
        }

        mPresentView.compatDrawable(R.drawable.yellowball)?.let { draw ->
            colorBallMap[Constants.COLOR_YELLOW] = draw
        }
        BitmapFactory.decodeResource(resources, R.drawable.yellowball)?.let { bm ->
            mPresentView.bitmapToDrawable(bm, nextBallWidth, nextBallHeight)?.let { draw ->
                colorNextBallMap[Constants.COLOR_YELLOW] = draw
            }
            mPresentView.bitmapToDrawable(bm, ovalBallWidth, ovalBallHeight)?.let { draw ->
                colorOvalBallMap[Constants.COLOR_YELLOW] = draw
            }
        }

        mPresentView.compatDrawable(R.drawable.cyanball)?.let { draw ->
            colorBallMap[Constants.COLOR_CYAN] = draw
        }
        BitmapFactory.decodeResource(resources, R.drawable.cyanball)?.let { bm ->
            mPresentView.bitmapToDrawable(bm, nextBallWidth, nextBallHeight)?.let { draw ->
                colorNextBallMap[Constants.COLOR_CYAN] = draw
            }
            mPresentView.bitmapToDrawable(bm, ovalBallWidth, ovalBallHeight)?.let { draw ->
                colorOvalBallMap[Constants.COLOR_CYAN] = draw
            }
        }
    }

    private fun gameOver() {
        mPresentView.showGameOverDialog()
    }

    private fun calculateScore(linkedLine: HashSet<Point>?): Int {
        if (linkedLine == null) {
            return 0
        }
        val numBalls = intArrayOf(0, 0, 0, 0, 0, 0)
        for (point in linkedLine) {
            when (mGridData.getCellValue(point.x, point.y)) {
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
        if (!mGameProp.isEasyLevel) {
            // difficult level
            totalScore *= 2 // double of easy level
        }

        return totalScore
    }

    private fun drawBall(imageView: ImageView, color: Int) {
        imageView.setImageDrawable(colorBallMap[color])
    }

    private fun drawOval(imageView: ImageView, color: Int) {
        imageView.setImageDrawable(colorOvalBallMap[color])
    }

    private fun drawNextBall(imageView: ImageView?, color: Int) {
        Log.d(TAG, "drawNextBall.imageView = $imageView")
        Log.d(TAG, "drawNextBall.color = $color")
        imageView?.let {
            mGameProp.apply {
                if (hasNextBall) {
                    it.setImageDrawable(colorNextBallMap[color])
                } else {
                    it.setImageDrawable(null)
                }
            }
        }
    }

    private fun displayNextBallsView() {
        // display the view of next balls
        Log.d(TAG, "displayNextBallsView")
        try {
            for ((key, value) in mGridData.getNextCellIndices()) {
                val imageView = mPresentView.getImageViewById(getImageId(key.x, key.y))
                drawNextBall(imageView, value)
            }
        } catch (ex: Exception) {
            Log.d(TAG, "displayNextBallsView.Exception: ")
            ex.printStackTrace()
        }
    }

    private fun displayNextColorBalls() {
        if (mGridData.randCells() == 0) {
            // no vacant, so game over
            gameOver()
            return
        }
        //   display the balls on the nextBallsView
        displayNextBallsView()
    }

    private fun clearCell(i: Int, j: Int) {
        mPresentView.getImageViewById(getImageId(i, j)).setImageBitmap(null)
        mGridData.setCellValue(i, j, 0)
    }

    private fun displayGridDataNextCells() {
        Log.d(TAG, "displayGridDataNextCells")
        var n1: Int
        var n2: Int
        var imageView: ImageView
        var hasMoreFive = false
        val linkedPoint = HashSet<Point>()
        for ((key, value) in mGridData.getNextCellIndices()) {
            n1 = key.x
            n2 = key.y
            mGridData.setCellValue(n1, n2, value)
            imageView = mPresentView.getImageViewById(getImageId(n1, n2))
            println("displayGridDataNextCells.imageView = $imageView")
            drawBall(imageView, mGridData.getCellValue(n1, n2))
            if (mGridData.checkMoreThanFive(n1, n2)) {
                hasMoreFive = true
                for (point in mGridData.getLightLine()) {
                    if (!linkedPoint.contains(point)) {
                        linkedPoint.add(Point(point))
                    }
                }
            }
        }

        if (hasMoreFive) {
            mGridData.setLightLine(linkedPoint) // added on 2020-07-13
            mGameProp.lastGotScore = calculateScore(mGridData.getLightLine())
            mGameProp.undoScore = mGameProp.currentScore
            mGameProp.currentScore += mGameProp.lastGotScore
            mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
            val showScore = ShowScore(
                mGridData.getLightLine(), mGameProp.lastGotScore,
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

    private fun displayGameGridView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView")
        try {
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    mPresentView.getImageViewById(getImageId(i, j)).let { imageV ->
                        mGridData.getCellValue(i, j).let { color ->
                            if (color == 0) {
                                // imageView.setImageDrawable(null);
                                imageV.setImageBitmap(null)
                            } else {
                                drawBall(imageV, color)
                            }
                        }
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.d(TAG, "displayGameGridView.Exception: ")
            ex.printStackTrace()
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
        val sizeOfPath = mGridData.getPathPoint().size
        if (sizeOfPath == 0) {
            Log.w(TAG, "drawBallAlongPath.sizeOfPathPoint == 0")
            return
        }
        val targetI = mGridData.getPathPoint()[0].x // the target point
        val targetJ = mGridData.getPathPoint()[0].y // the target point
        Log.d(TAG, "drawBallAlongPath.targetI = $targetI, targetJ = $targetJ")
        val beginI = mGridData.getPathPoint()[sizeOfPath - 1].x
        val beginJ = mGridData.getPathPoint()[sizeOfPath - 1].y
        val color = mGridData.getCellValue(beginI, beginJ)
        Log.d(TAG, "drawBallAlongPath.color = $color")

        val tempList = ArrayList(mGridData.getPathPoint())
        val runnablePath: Runnable = object : Runnable {
            var ballYN: Boolean = true
            var countDown: Int = tempList.size * 2 - 1
            @Synchronized
            override fun run() {
                mGameProp.threadCompleted[0] = false
                mGameProp.isBallMoving = true
                if (countDown >= 2) {   // eliminate start point
                    val i = countDown / 2
                    mPresentView.getImageViewById(getImageId(tempList[i].x, tempList[i].y)).let { imageV ->
                        if (ballYN) {
                            drawBall(imageV, color)
                        } else {
                            imageV.setImageBitmap(null)
                        }
                    }
                    ballYN = !ballYN
                    countDown--
                    movingBallHandler.postDelayed(this, 20)
                } else {
                    clearCell(beginI, beginJ) // blank the original cell. Added on 2020-09-16
                    val v = mPresentView.getImageViewById(getImageId(targetI, targetJ))
                    mGridData.setCellValue(targetI, targetJ, color)
                    drawBall(v, color)
                    mGridData.regenerateNextCellIndices(Point(targetI, targetJ))
                    //  check if there are more than five balls with same color connected together
                    if (mGridData.checkMoreThanFive(targetI, targetJ)) {
                        mGameProp.lastGotScore = calculateScore(mGridData.getLightLine())
                        mGameProp.undoScore = mGameProp.currentScore
                        mGameProp.currentScore += mGameProp.lastGotScore
                        mPresentView.updateCurrentScoreOnUi(mGameProp.currentScore)
                        Log.d(TAG, "drawBallAlongPath.showScore")
                        val showScore = ShowScore(
                            mGridData.getLightLine(), mGameProp.lastGotScore,
                            false, object : ShowScoreCallback {
                                override fun sCallback() {
                                    Log.d(TAG, "drawBallAlongPath.ShowScoreCallback.sCallback")
                                    mGameProp.threadCompleted[0] = true
                                    mGameProp.isBallMoving = false
                                    Log.d(TAG, "drawBallAlongPath.run() finished.")
                                }
                            })
                        Log.d(TAG, "drawBallAlongPath.showScore()")
                        showingScoreHandler.post(showScore)
                    } else {
                        displayGridDataNextCells() // has a problem
                        mGameProp.threadCompleted[0] = true
                        mGameProp.isBallMoving = false
                        Log.d(TAG, "drawBallAlongPath.run() finished.")
                    }
                }
            }
        }
        movingBallHandler.post(runnablePath)
    }

    private fun drawBouncyBall(v: ImageView?, color: Int) {
        Log.e(TAG, "drawBouncyBall")
        if (v == null) {
            Log.e(TAG, "drawBouncyBall.v is null, color = $color")
            return
        }
        bouncyAnimation = AnimationDrawable()
        bouncyAnimation?.let {
            it.isOneShot = false
            colorBallMap[color]?.let { draw ->
                it.addFrame(draw, 200)
            }
            colorOvalBallMap[color]?.let { draw ->
                it.addFrame(draw, 200)
            }
            v.setImageDrawable(it)
            it.start()
        }
    }

    private fun stopBouncyAnimation() {
        bouncyAnimation?.let {
            if (it.isRunning) {
                it.stop()
            }
        }
    }

    fun getImageId(row: Int, column: Int): Int {
        // Log.d(TAG, "getImageId.row = " + row + ", column = " + column );
        return row * rowCounts + column
    }

    private fun getRow(imageId: Int): Int {
        return imageId / rowCounts
    }

    private fun getColumn(imageId: Int): Int {
        return imageId % rowCounts
    }

    private inner class ShowScore(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int, val isNextBalls: Boolean,
        val callback: ShowScoreCallback): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            Log.d(TAG, "ShowScore")
            pointSet = HashSet(linkedPoint)
            mGameProp.isShowNextBallsAfterBlinking = isNextBalls
            mGameProp.threadCompleted[1] = false
            mGameProp.isShowingScoreMessage = true
        }

        @Synchronized
        private fun onProgressUpdate(status: Int) {
            when (status) {
                0 -> for (item in pointSet) {
                    val v = mPresentView.getImageViewById(getImageId(item.x, item.y))
                    drawBall(v, mGridData.getCellValue(item.x, item.y))
                }
                1 -> for (item in pointSet) {
                    val v = mPresentView.getImageViewById(getImageId(item.x, item.y))
                    drawOval(v, mGridData.getCellValue(item.x, item.y))
                }
                2 -> {}
                3 -> {
                    // show the score
                    val scoreString = lastGotScore.toString()
                    mPresentView.showMessageOnScreen(scoreString)
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                    }
                    if (isNextBalls) {
                        Log.d(TAG, "ShowScore.onProgressUpdate.displayNextColorBalls")
                        displayNextColorBalls()
                    } else {
                        Log.d(TAG, "ShowScore.onProgressUpdate.displayNextBallsView")
                        displayNextBallsView()
                    }
                    mGameProp.threadCompleted[1] = true // user can start input command
                    mGameProp.isShowingScoreMessage = false
                }
                4 -> {
                    Log.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                    mPresentView.dismissShowMessageOnScreen()
                }
                else -> {}
            }
        }

        @Synchronized
        override fun run() {
            val twinkleCountDown = 5
            mCounter++
            Log.d(TAG, "ShowScore.run().mCounter = $mCounter")
            if (mCounter <= twinkleCountDown) {
                val md = mCounter % 2 // modulus
                onProgressUpdate(md)
                showingScoreHandler.postDelayed(this, 100)
            } else {
                if (mCounter == twinkleCountDown + 1) {
                    onProgressUpdate(3) // show score
                    showingScoreHandler.postDelayed(this, 500)
                } else {
                    showingScoreHandler.removeCallbacksAndMessages(null)
                    onProgressUpdate(4) // dismiss showing message
                    mGameProp.threadCompleted[1] = true
                    mGameProp.isShowingScoreMessage = false
                    callback.sCallback()
                }
            }
        }
    }

    companion object {
        private const val TAG = "Presenter"
        private const val NUM_SAVE_FILENAME = "NumSavedGame"
        private const val SAVE_FILENAME = "SavedGame"
    }
}