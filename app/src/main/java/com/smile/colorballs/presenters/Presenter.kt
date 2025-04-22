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
    private var mRowCounts = 0
    private var mColCounts = 0
    private var mGameProp: GameProp? = null
    private var mGridData: GridData? = null
    private val movingBallHandler = Handler(Looper.getMainLooper())
    private val showingScoreHandler = Handler(Looper.getMainLooper())

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

    fun setRowCounts(rowCounts: Int) {
        mRowCounts = rowCounts
    }

    fun setColCounts(colCounts: Int) {
        mColCounts = colCounts
    }

    fun drawBallsAndCheckListener(v: View) {
        Log.d(TAG, "drawBallsAndCheckListener")
        val id = v.id
        var i = getRow(id)
        var j = getColumn(id)
        mGameProp?.let { prop ->
            mGridData?.let { grid ->
                Log.d(TAG, "drawBallsAndCheckListener.isBallBouncing = ${prop.isBallBouncing}")
                if (!prop.isBallBouncing) {
                    if (grid.getCellValue(i, j) != 0) {
                        if ((prop.bouncyBallIndexI == -1) && (prop.bouncyBallIndexJ == -1)) {
                            prop.isBallBouncing = true
                            drawBouncyBall(v as ImageView, grid.getCellValue(i, j))
                            prop.bouncyBallIndexI = i
                            prop.bouncyBallIndexJ = j
                        }
                    }
                } else {
                    // cancel the bouncy timer
                    val bouncyI = prop.bouncyBallIndexI
                    val bouncyJ = prop.bouncyBallIndexJ
                    if (grid.getCellValue(i, j) == 0) {
                        //   blank cell
                        if ((bouncyI >= 0) && (bouncyI >= 0)) {
                            if (grid.canMoveCellToCell(Point(bouncyI, bouncyJ), Point(i, j))) {
                                // cancel the timer
                                prop.isBallBouncing = false
                                stopBouncyAnimation()
                                prop.bouncyBallIndexI = -1
                                prop.bouncyBallIndexJ = -1
                                drawBallAlongPath()
                                prop.undoEnable = true
                            } else {
                                //  make a sound
                                if (prop.hasSound) {
                                    soundPool.playSound()
                                }
                            }
                        }
                    } else {
                        //  cell is not blank
                        if ((bouncyI >= 0) && (bouncyJ >= 0)) {
                            stopBouncyAnimation()
                            val imageView = mPresentView.getImageViewById(getImageId(bouncyI, bouncyJ))
                            drawBall(imageView, grid.getCellValue(bouncyI, bouncyJ))
                            drawBouncyBall(v as ImageView, grid.getCellValue(i, j))
                            prop.bouncyBallIndexI = i
                            prop.bouncyBallIndexJ = j
                        }
                    }
                }
            }
        }
    }

    fun initGame(cellWidth: Int, cellHeight: Int, state: Bundle?): Boolean {
        bitmapDrawableResources(cellWidth, cellHeight)
        val highestScore = mPresentView.highestScore()
        var isNewGame = true
        state?.let {
            Log.d(TAG,"initGame.Configuration changed and restore the original UI.")
            mGameProp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33
                BundleCompat.getParcelable(state, GAME_PROP_TAG, GameProp::class.java)
            } else state.getParcelable(GAME_PROP_TAG)
            mGameProp?.let { prop ->
                mGridData = prop.gridData
                isNewGame = false
            }
        } ?: run {
            // activity just started so new game
            Log.d(TAG, "initGame.state is null, new game")
            mGridData = GridData(mRowCounts, mColCounts, NUM_EASY)
            mGameProp = GameProp(mGridData!!)
        }
        mGameProp?.let {
            ColorBallsApp.isShowingLoadingMessage = it.isShowingLoadingMessage
            ColorBallsApp.isProcessingJob = it.isProcessingJob
            mPresentView.updateHighestScoreOnUi(highestScore)
            mPresentView.updateCurrentScoreOnUi(it.currentScore)
        }

        displayGameView()
        if (isNewGame) {
            displayGridDataNextCells()
        } else {
            // display the original state before changing configuration
            // need to be tested
            if (ColorBallsApp.isShowingLoadingMessage) {
                mPresentView.showLoadingStrOnScreen()
            }
            //
            mGameProp?.let {
                if (it.isBallMoving) {
                    Log.d(TAG, "initGame.gameProp.isBallMoving() is true")
                    drawBallAlongPath()
                }
                if (it.isShowingScoreMessage) {
                    Log.d(TAG, "initGame.gameProp.isShowingScoreMessage() is true")
                    mGridData?.let { grid ->
                        val showScore = ShowScore(
                            grid.getLightLine(), it.lastGotScore,
                            it.isShowNextBallsAfterBlinking,
                            object : ShowScoreCallback {
                                override fun sCallback() {
                                    lastPartOfInitialGame()
                                }
                            })
                        Log.d(TAG, "initGame.showingScoreHandler.post().")
                        showingScoreHandler.post(showScore)
                    }
                } else {
                    lastPartOfInitialGame()
                }
            }
        }
        return isNewGame
    }

    private fun lastPartOfInitialGame() {
        mGameProp?.let {
            if (it.isBallBouncing) {
                mGridData?.let { grid ->
                    val i = it.bouncyBallIndexI
                    val j = it.bouncyBallIndexJ
                    val v = mPresentView.getImageViewById(getImageId(i, j))
                    drawBouncyBall(v, grid.getCellValue(i, j))
                }
            }
            if (it.isShowingNewGameDialog) {
                Log.d(TAG, "lastPartOfInitialGame.newGame()")
                newGame()
            }
            if (it.isShowingQuitGameDialog) {
                Log.d(TAG, "lastPartOfInitialGame.show quitGame()")
                quitGame()
            }
            if (it.isShowingSureSaveDialog) {
                Log.d(TAG, "lastPartOfInitialGame.saveGame()")
                saveGame()
            }
            if (it.isShowingSureLoadDialog) {
                Log.d(TAG, "lastPartOfInitialGame.loadGame()")
                loadGame()
            }
            if (it.isShowingGameOverDialog) {
                Log.d(TAG, "lastPartOfInitialGame.gameOver()")
                gameOver()
            }
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        mGameProp?.let {
            it.isShowingLoadingMessage = ColorBallsApp.isShowingLoadingMessage
            it.isProcessingJob = ColorBallsApp.isProcessingJob
        }
        outState.putParcelable(GAME_PROP_TAG, mGameProp)
    }

    fun completedAll(): Boolean {
        mGameProp?.let {
            for (thCompleted in it.threadCompleted) {
                if (!thCompleted) {
                    return false
                }
            }
        }
        return true
    }

    fun hasSound(): Boolean {
        mGameProp?.let {
            return it.hasSound
        }
        return false
    }

    fun setHasSound(hasSound: Boolean) {
        mGameProp?.let {
            it.hasSound = hasSound
        }
    }

    fun isEasyLevel(): Boolean {
        mGameProp?.let {
            return it.isEasyLevel
        }
        return true
    }

    fun setEasyLevel(yn: Boolean) {
        mGameProp?.let {
            it.isEasyLevel = yn
            mGridData?.setNumOfColorsUsed(if (yn) NUM_EASY else NUM_DIFFICULT)
        }
    }

    fun hasNextBall(): Boolean {
        mGameProp?.let {
            return it.hasNextBall
        }
        return true
    }

    fun setHasNextBall(hasNextBall: Boolean, isNextBalls: Boolean) {
        mGameProp?.let {
            it.hasNextBall = hasNextBall
        }
        if (isNextBalls) {
            displayNextBallsView()
        }
    }

    private fun setShowingNewGameDialog(showingNewGameDialog: Boolean) {
        mGameProp?.isShowingNewGameDialog = showingNewGameDialog
    }

    private fun setShowingQuitGameDialog(showingQuitGameDialog: Boolean) {
        mGameProp?.isShowingQuitGameDialog = showingQuitGameDialog
    }

    fun undoTheLast() {
        mGameProp?.let {
            if (!it.undoEnable) {
                return
            }
            ColorBallsApp.isProcessingJob = true // started undoing
            mGridData?.undoTheLast()
            stopBouncyAnimation()
            it.isBallBouncing = false
            it.bouncyBallIndexI = -1
            it.bouncyBallIndexJ = -1
            // restore the screen
            displayGameView()
            it.currentScore = it.undoScore
            mPresentView.updateCurrentScoreOnUi(it.currentScore)
            // completedPath = true;
            it.undoEnable = false
            ColorBallsApp.isProcessingJob = false // finished
        }
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

        mGameProp?.isShowingSureSaveDialog = isShowingSureSaveDialog
    }

    fun setShowingSureLoadDialog(isShowingSureLoadDialog: Boolean) {
        mGameProp?.isShowingSureLoadDialog = isShowingSureLoadDialog
    }

    fun setShowingGameOverDialog(isShowingGameOverDialog: Boolean) {
        mGameProp?.isShowingGameOverDialog = isShowingGameOverDialog
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
        mGameProp?.let {
            mPresentView.showSaveScoreAlertDialog(1, it.currentScore)
        }
    }

    fun quitGame() {
        mGameProp?.let {
            mPresentView.showSaveScoreAlertDialog(0, it.currentScore)
        }
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
            mGameProp?.let {
                mGridData?.let { grid ->
                    Log.d(TAG, "startSavingGame.hasSound = " + it.hasSound)
                    if (it.hasSound) foStream.write(1) else foStream.write(0)
                    Log.d(TAG, "startSavingGame.isEasyLevel = " + it.isEasyLevel)
                    if (it.isEasyLevel) foStream.write(1) else foStream.write(0)
                    Log.d(TAG, "startSavingGame.hasNextBall = " + it.hasNextBall)
                    if (it.hasNextBall) foStream.write(1) else foStream.write(0)
                    // save next balls
                    // foStream.write(gridData.ballNumOneTime);
                    Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
                    foStream.write(Constants.BALL_NUM_ONE_TIME)
                    for ((_, value) in grid.getNextCellIndices()) {
                        Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = $value")
                        foStream.write(value)
                    }
                    var sz = grid.getNextCellIndices().size
                    for (i in sz until NUM_DIFFICULT) {
                        Log.d(TAG, "startSavingGame.nextCellIndices.getValue() = " + 0)
                        foStream.write(0)
                    }
                    Log.d(TAG, "startSavingGame.getNextCellIndices.size() = $sz")
                    foStream.write(sz)
                    for ((key) in grid.getNextCellIndices()) {
                        Log.d(TAG, "startSavingGame.nextCellIndices.getKey().x = " + key.x)
                        Log.d(TAG, "startSavingGame.nextCellIndices.getKey().y = " + key.y)
                        foStream.write(key.x)
                        foStream.write(key.y)
                    }
                    Log.d(TAG,"startSavingGame.getUndoNextCellIndices().size() = "
                            + grid.getUndoNextCellIndices().size)
                    foStream.write(grid.getUndoNextCellIndices().size)
                    for ((key) in grid.getUndoNextCellIndices()) {
                        Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().x = " + key.x)
                        Log.d(TAG, "startSavingGame.undoNextCellIndices.getKey().y = " + key.y)
                        foStream.write(key.x)
                        foStream.write(key.y)
                    }
                    // save values on 9x9 grid
                    for (i in 0 until mRowCounts) {
                        for (j in 0 until mColCounts) {
                            Log.d(TAG,"startSavingGame.gridData.getCellValue(i, j) = "
                                    + grid.getCellValue(i, j))
                            foStream.write(grid.getCellValue(i, j))
                        }
                    }
                    // save current score
                    val scoreByte = ByteBuffer.allocate(4).putInt(it.currentScore).array()
                    Log.d(TAG, "startSavingGame.scoreByte = $scoreByte")
                    foStream.write(scoreByte)
                    // save undoEnable
                    Log.d(TAG, "startSavingGame.isUndoEnable = " + it.undoEnable)
                    // can undo or no undo
                    if (it.undoEnable) foStream.write(1) else foStream.write(0)
                    Log.d(TAG, "startSavingGame.ballNumOneTime = " + Constants.BALL_NUM_ONE_TIME)
                    foStream.write(Constants.BALL_NUM_ONE_TIME)
                    // save undoNextBalls
                    for ((_, value) in grid.getUndoNextCellIndices()) {
                        Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = $value")
                        foStream.write(value)
                    }
                    sz = grid.getUndoNextCellIndices().size
                    for (i in sz until NUM_DIFFICULT) {
                        Log.d(TAG, "startSavingGame.undoNextCellIndices.getValue() = " + 0)
                        foStream.write(0)
                    }
                    // save backupCells
                    for (i in 0 until mRowCounts) {
                        for (j in 0 until mColCounts) {
                            Log.d(TAG,"startSavingGame.gridData.getBackupCells()[i][j] = "
                                    + grid.getBackupCells()[i][j])
                            foStream.write(grid.getBackupCells()[i][j])
                        }
                    }
                    val undoScoreByte = ByteBuffer.allocate(4).putInt(it.undoScore).array()
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
                }
            }
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
        var hasSound: Boolean
        var isEasyLevel: Boolean
        var hasNextBall: Boolean
        var ballNumOneTime: Int
        val nextBalls = IntArray(NUM_DIFFICULT)
        val gameCells = Array(mRowCounts) { IntArray(mColCounts) }
        var cScore: Int
        var isUndoEnable: Boolean
        val undoNextBalls = IntArray(NUM_DIFFICULT)
        val backupCells = Array(mRowCounts) { IntArray(mColCounts) }
        mGameProp?.let {
            mGridData?.let { grid ->
                var unScore = it.undoScore
                try {
                    // clear nextCellIndices and undoNextCellIndices
                    grid.setNextCellIndices(HashMap())
                    grid.setUndoNextCellIndices(HashMap())
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
                    for (i in 0 until NUM_DIFFICULT) {
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
                        grid.addNextCellIndices(Point(x, y))
                    }
                    val undoNextCellIndicesSize = fiStream.read()
                    Log.d(TAG,"startLoadingGame.getUndoNextCellIndices.size() = " +
                            "$undoNextCellIndicesSize")
                    for (i in 0 until undoNextCellIndicesSize) {
                        val x = fiStream.read()
                        val y = fiStream.read()
                        Log.d(TAG, "startLoadingGame.undoNextCellIndices.getKey().x = $x")
                        Log.d(TAG, "startLoadingGame.undoNextCellIndices.geyKey().y = $y")
                        grid.addUndoNextCellIndices(Point(x, y))
                    }
                    // load values on 9x9 grid
                    for (i in 0 until mRowCounts) {
                        for (j in 0 until mColCounts) {
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
                        for (i in 0 until NUM_DIFFICULT) {
                            undoNextBalls[i] = fiStream.read()
                            Log.d(TAG,"startLoadingGame.undoNextCellIndices.getValue() = "
                                    + undoNextBalls[i])
                        }
                        // save backupCells
                        for (i in 0 until mRowCounts) {
                            for (j in 0 until mColCounts) {
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
                    for (entry in grid.getNextCellIndices().entries) {
                        entry.setValue(nextBalls[kk++])
                    }
                    grid.setCellValues(gameCells)
                    it.currentScore = cScore
                    it.undoEnable = isUndoEnable
                    kk = 0
                    for (entry in grid.getUndoNextCellIndices().entries) {
                        entry.setValue(undoNextBalls[kk++])
                    }
                    grid.setBackupCells(backupCells)
                    it.undoScore = unScore
                    // start update UI
                    mPresentView.updateCurrentScoreOnUi(it.currentScore)
                    Log.d(TAG, "startLoadingGame.starting displayGameView().")
                    displayGameView()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    succeeded = false
                }
            }
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
            mGridData?.let {
                when (it.getCellValue(point.x, point.y)) {
                    Constants.COLOR_RED -> numBalls[0]++
                    Constants.COLOR_GREEN -> numBalls[1]++
                    Constants.COLOR_BLUE -> numBalls[2]++
                    Constants.COLOR_MAGENTA -> numBalls[3]++
                    Constants.COLOR_YELLOW -> numBalls[4]++
                    Constants.COLOR_CYAN -> numBalls[5]++
                    else -> {}
                }
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
        mGameProp?.let {
            if (!it.isEasyLevel) {
                // difficult level
                totalScore *= 2 // double of easy level
            }
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
            mGameProp?.apply {
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
        mGridData?.let { grid ->
            try {
                for ((key, value) in grid.getNextCellIndices()) {
                    val imageView = mPresentView.getImageViewById(getImageId(key.x, key.y))
                    drawNextBall(imageView, value)
                }
            } catch (ex: Exception) {
                Log.d(TAG, "displayNextBallsView.Exception: ")
                ex.printStackTrace()
            }
        }
    }

    private fun displayNextColorBalls() {
        mGridData?.let { grid ->
            if (grid.randCells() == 0) {
                // no vacant, so game over
                gameOver()
                return
            }
        }
        //   display the balls on the nextBallsView
        displayNextBallsView()
    }

    private fun clearCell(i: Int, j: Int) {
        mPresentView.getImageViewById(getImageId(i, j)).setImageBitmap(null)
        mGridData?.setCellValue(i, j, 0)
    }

    private fun displayGridDataNextCells() {
        Log.d(TAG, "displayGridDataNextCells")
        mGameProp?.let { prop ->
            mGridData?.let { grid ->
                var n1: Int
                var n2: Int
                var imageView: ImageView
                var hasMoreFive = false
                val linkedPoint = HashSet<Point>()
                for ((key, value) in grid.getNextCellIndices()) {
                    n1 = key.x
                    n2 = key.y
                    grid.setCellValue(n1, n2, value)
                    imageView = mPresentView.getImageViewById(getImageId(n1, n2))
                    drawBall(imageView, grid.getCellValue(n1, n2))
                    if (grid.checkMoreThanFive(n1, n2)) {
                        hasMoreFive = true
                        for (point in grid.getLightLine()) {
                            if (!linkedPoint.contains(point)) {
                                linkedPoint.add(Point(point))
                            }
                        }
                    }
                }

                if (hasMoreFive) {
                    grid.setLightLine(linkedPoint) // added on 2020-07-13
                    prop.lastGotScore = calculateScore(grid.getLightLine())
                    prop.undoScore = prop.currentScore
                    prop.currentScore += prop.lastGotScore
                    mPresentView.updateCurrentScoreOnUi(prop.currentScore)
                    val showScore = ShowScore(
                        grid.getLightLine(), prop.lastGotScore,
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
        }
    }

    private fun displayGameGridView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView")
        mGridData?.let { grid ->
            try {
                for (i in 0 until mRowCounts) {
                    for (j in 0 until mColCounts) {
                        mPresentView.getImageViewById(getImageId(i, j)).let { imageV ->
                            grid.getCellValue(i, j).let { color ->
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
    }

    private fun displayGameView() {
        // display the 9 x 9 game view
        Log.d(TAG, "displayGameView")
        displayGameGridView()
        // display the view of next balls
        displayNextBallsView()
    }

    private fun drawBallAlongPath() {
        mGameProp?.let { prop ->
            mGridData?.let { grid ->
                val sizeOfPath = grid.getPathPoint().size
                if (sizeOfPath == 0) {
                    Log.w(TAG, "drawBallAlongPath.sizeOfPathPoint == 0")
                    return
                }
                val targetI = grid.getPathPoint()[0].x // the target point
                val targetJ = grid.getPathPoint()[0].y // the target point
                Log.d(TAG, "drawBallAlongPath.targetI = $targetI, targetJ = $targetJ")
                val beginI = grid.getPathPoint()[sizeOfPath - 1].x
                val beginJ = grid.getPathPoint()[sizeOfPath - 1].y
                val color = grid.getCellValue(beginI, beginJ)
                Log.d(TAG, "drawBallAlongPath.color = $color")

                val tempList = ArrayList(grid.getPathPoint())
                val runnablePath: Runnable = object : Runnable {
                    var ballYN: Boolean = true
                    var countDown: Int = tempList.size * 2 - 1
                    @Synchronized
                    override fun run() {
                        prop.threadCompleted[0] = false
                        prop.isBallMoving = true
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
                            grid.setCellValue(targetI, targetJ, color)
                            drawBall(v, color)
                            grid.regenerateNextCellIndices(Point(targetI, targetJ))
                            //  check if there are more than five balls with same color connected together
                            if (grid.checkMoreThanFive(targetI, targetJ)) {
                                prop.lastGotScore = calculateScore(grid.getLightLine())
                                prop.undoScore = prop.currentScore
                                prop.currentScore += prop.lastGotScore
                                mPresentView.updateCurrentScoreOnUi(prop.currentScore)
                                Log.d(TAG, "drawBallAlongPath.showScore")
                                val showScore = ShowScore(
                                    grid.getLightLine(), prop.lastGotScore,
                                    false, object : ShowScoreCallback {
                                        override fun sCallback() {
                                            Log.d(TAG, "drawBallAlongPath.ShowScoreCallback.sCallback")
                                            prop.threadCompleted[0] = true
                                            prop.isBallMoving = false
                                            Log.d(TAG, "drawBallAlongPath.run() finished.")
                                        }
                                    })
                                Log.d(TAG, "drawBallAlongPath.showScore()")
                                showingScoreHandler.post(showScore)
                            } else {
                                displayGridDataNextCells() // has a problem
                                prop.threadCompleted[0] = true
                                prop.isBallMoving = false
                                Log.d(TAG, "drawBallAlongPath.run() finished.")
                            }
                        }
                    }
                }
                movingBallHandler.post(runnablePath)
            }
        }
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
        return row * mRowCounts + column
    }

    private fun getRow(imageId: Int): Int {
        return imageId / mRowCounts
    }

    private fun getColumn(imageId: Int): Int {
        return imageId % mRowCounts
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
            mGameProp?.let { prop ->
                prop.isShowNextBallsAfterBlinking = isNextBalls
                prop.threadCompleted[1] = false
                prop.isShowingScoreMessage = true
            }
        }

        @Synchronized
        private fun onProgressUpdate(status: Int) {
            mGridData?.let { grid ->
                when (status) {
                    0 -> for (item in pointSet) {
                        val v = mPresentView.getImageViewById(getImageId(item.x, item.y))
                        drawBall(v, grid.getCellValue(item.x, item.y))
                    }
                    1 -> for (item in pointSet) {
                        val v = mPresentView.getImageViewById(getImageId(item.x, item.y))
                        drawOval(v, grid.getCellValue(item.x, item.y))
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
                        mGameProp?.let { prop ->
                            prop.threadCompleted[1] = true // user can start input command
                            prop.isShowingScoreMessage = false
                        }
                    }
                    4 -> {
                        Log.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                        mPresentView.dismissShowMessageOnScreen()
                    }
                    else -> {}
                }
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
                    mGameProp?.let { prop ->
                        prop.threadCompleted[1] = true
                        prop.isShowingScoreMessage = false
                    }
                    callback.sCallback()
                }
            }
        }
    }

    companion object {
        private const val TAG = "Presenter"
        // 5 colors for easy level
        private const val NUM_EASY = 5
        // 6 colors for difficult level
        private const val NUM_DIFFICULT = 6
        private const val NUM_SAVE_FILENAME = "NumSavedGame"
        private const val GAME_PROP_TAG = "GameProp"
        private const val SAVE_FILENAME = "SavedGame"
    }
}