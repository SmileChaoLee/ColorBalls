package com.smile.colorballs.viewmodel

import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smile.colorballs.ballsremover.constants.BallsRmConstants
import com.smile.colorballs.models.GridData
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.colorballs.constants.WhichGame
import com.smile.colorballs.models.ColorBallInfo
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.presenters.BasePresenter
import com.smile.colorballs.roomdatabase.Score
import com.smile.colorballs.tools.Utils
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.utilities.SoundPoolUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

abstract class BaseViewModel(
    private val basePresenter: BasePresenter): ViewModel() {

    abstract fun initProperties(): String   // for test only
    abstract fun initGame(bundle: Bundle?)
    abstract fun cellClickListener(i: Int, j: Int)
    abstract fun startSavingGame(): Boolean
    abstract fun startLoadingGame(): Boolean

    interface ShowScoreCallback {
        fun sCallback()
    }

    private val showingScoreHandler = Handler(Looper.getMainLooper())
    private var loadingStr = ""
    var savingGameStr = ""
    var loadingGameStr = ""
    private var sureToSaveGameStr = ""
    private var sureToLoadGameStr = ""
    var saveScoreStr = ""
    lateinit var soundPool: SoundPoolUtil

    protected lateinit var mGameProp: GameProp
    protected lateinit var mGridData: GridData
    var mGameAction = Constants.IS_QUITING_GAME
    var rowCounts: Int = BallsRmConstants.ROW_COUNTS
        private set(value) {
            if (value != field) field = value
        }
    var colCounts: Int = BallsRmConstants.COLUMN_COUNTS
        private set(value) {
            if (value != field) field = value
        }

    private val currentScore = mutableIntStateOf(0)
    fun getCurrentScore() = currentScore.intValue
    fun setCurrentScore(score: Int) {
        currentScore.intValue = score
    }

    private val highestScore = mutableIntStateOf(0)
    fun getHighestScore() = highestScore.intValue
    fun setHighestScore(score: Int) {
        highestScore.intValue = score
    }

    private val screenMessage = mutableStateOf("")
    fun getScreenMessage() = screenMessage.value
    fun setScreenMessage(msg: String) {
        screenMessage.value = msg
    }

    private val saveGameText = mutableStateOf("")
    fun getSaveGameText() = saveGameText.value
    fun setSaveGameText(text: String) {
        saveGameText.value = text
    }

    private val loadGameText = mutableStateOf("")
    fun getLoadGameText() = loadGameText.value
    fun setLoadGameText(text: String) {
        loadGameText.value = text
    }

    private val saveScoreTitle = mutableStateOf("")
    fun getSaveScoreTitle() = saveScoreTitle.value
    fun setSaveScoreTitle(title: String) {
        saveScoreTitle.value = title
    }

    var gridDataArray = Array(BallsRmConstants.ROW_COUNTS) {
        Array(BallsRmConstants.COLUMN_COUNTS) {
            mutableStateOf(ColorBallInfo())
        }
    }

    init {
        Log.d(TAG, "BaseViewModel.init")
        Log.d(TAG, "initProperties() = ${initProperties()}")
    }

    fun setProperties() {
        rowCounts = mGridData.rowCounts
        colCounts = mGridData.colCounts
        loadingStr = basePresenter.loadingStr
        savingGameStr = basePresenter.savingGameStr
        loadingGameStr = basePresenter.loadingGameStr
        sureToSaveGameStr = basePresenter.sureToSaveGameStr
        sureToLoadGameStr = basePresenter.sureToLoadGameStr
        saveScoreStr = basePresenter.saveScoreStr
        soundPool = basePresenter.soundPool
        gridDataArray = Array(rowCounts) {
            Array(colCounts) {
                mutableStateOf(ColorBallInfo())
            }
        }
    }

    fun setWhichGame(whichGame: WhichGame) {
        mGameProp.whichGame = whichGame
    }

    fun getWhichGame(): WhichGame {
        return mGameProp.whichGame
    }

    private fun getAndSetHighestScore() {
        Log.d(TAG, "getAndSetHighestScore")
        viewModelScope.launch(Dispatchers.IO) {
            val db = basePresenter.scoreDatabase()
            val score = db.getHighestScore()
            Log.d(TAG, "getAndSetHighestScore.score = $score")
            db.close()
            setHighestScore(score)
        }
    }

    private fun addScoreInLocalTop10(playerName : String, score : Int) {
        Log.d(TAG, "addScoreInLocalTop10")
        viewModelScope.launch(Dispatchers.IO) {
            val db = basePresenter.scoreDatabase()
            if (db.isInTop10(score)) {
                val scoreModel = Score(playerName = playerName, playerScore = score)
                val rowId = db.addScore(scoreModel)
                Log.d(TAG, "addScoreInLocalTop10.rowId = $rowId")
                db.deleteAllAfterTop10()
            }
            db.close()
            // get the highest score after adding
            getAndSetHighestScore()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState.mGridData = $mGridData")
        outState.putParcelable(Constants.GAME_PROP_TAG, mGameProp)
        outState.putParcelable(Constants.GRID_DATA_TAG, mGridData)
    }

    fun hasSound(): Boolean {
        return mGameProp.hasSound
    }
    fun setHasSound(hasSound: Boolean) {
        mGameProp.hasSound = hasSound
    }

    fun isEasyLevel(): Boolean {
        return mGameProp.isEasyLevel
    }
    fun setEasyLevel(easyLevel: Boolean) {
        mGameProp.isEasyLevel = easyLevel
        val num = if (easyLevel) Constants.NUM_BALLS_USED_EASY
        else Constants.NUM_BALLS_USED_DIFF
        mGridData.setNumOfColorsUsed(num)
    }

    fun hasNext(): Boolean {
        return mGameProp.hasNext
    }
    fun setHasNext(fillColumn: Boolean) {
        mGameProp.hasNext = fillColumn
    }

    fun saveScore(playerName: String) {
        Log.d(TAG, "saveScore")
        // use thread to add a record to remote database
        val restThread: Thread = object : Thread() {
            override fun run() {
                try {
                    // ASP.NET Core
                    val jsonObject = JSONObject()
                    jsonObject.put("PlayerName", playerName)
                    jsonObject.put("Score", mGameProp.currentScore)
                    jsonObject.put("GameId", Utils.getGameId(getWhichGame()))
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
        addScoreInLocalTop10(playerName, mGameProp.currentScore)
    }

    fun quitGame() {
        // quiting the game
        Log.d(TAG, "quitGame")
        mGameAction = Constants.IS_QUITING_GAME
        setSaveScoreTitle(saveScoreStr)
    }

    fun saveGame() {
        Log.d(TAG, "saveGame")
        setSaveGameText(sureToSaveGameStr)
    }

    fun loadGame() {
        Log.d(TAG, "loadGame")
        setLoadGameText(sureToLoadGameStr)
    }

    fun release() {
        showingScoreHandler.removeCallbacksAndMessages(null)
        soundPool.release()
    }

    fun drawBall(i: Int, j: Int, color: Int) {
        Log.d(TAG, "drawBall.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.BALL)
    }

    fun drawOval(i: Int, j: Int, color: Int) {
        Log.d(TAG, "drawOval.($i, $j), color = $color")
        gridDataArray[i][j].value = ColorBallInfo(color, WhichBall.OVAL_BALL)
    }

    fun clearCell(i: Int, j: Int) {
        mGridData.setCellValue(i, j, 0)
    }

    fun displayGameGridView() {
        Log.d(TAG, "displayGameGridView")
        try {
            for (i in 0 until rowCounts) {
                for (j in 0 until colCounts) {
                    val color = mGridData.getCellValue(i, j)
                    drawBall(i, j, color)
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.d(TAG, "displayGameGridView.Exception: ")
            ex.printStackTrace()
        }
    }

    inner class ShowScore(
        linkedPoint: HashSet<Point>,
        val lastGotScore: Int,
        val callback: ShowScoreCallback
    ): Runnable {
        private var pointSet: HashSet<Point>
        private var mCounter = 0
        init {
            Log.d(TAG, "ShowScore")
            pointSet = HashSet(linkedPoint)
        }

        @Synchronized
        private fun onProgressUpdate(status: Int) {
            when (status) {
                0 -> for (item in pointSet) {
                    drawBall(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                }
                1 -> for (item in pointSet) {
                    drawOval(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                }
                2 -> {}
                3 -> {
                    setScreenMessage(lastGotScore.toString())
                    for (item in pointSet) {
                        clearCell(item.x, item.y)
                        drawBall(item.x, item.y, mGridData.getCellValue(item.x, item.y))
                    }
                }
                4 -> {
                    Log.d(TAG, "ShowScore.onProgressUpdate.dismissShowMessageOnScreen.")
                    setScreenMessage("")
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
                showingScoreHandler.postDelayed(this, 150)
            } else {
                if (mCounter == twinkleCountDown + 1) {
                    onProgressUpdate(3) // show score
                    showingScoreHandler.postDelayed(this, 500)
                } else {
                    showingScoreHandler.removeCallbacksAndMessages(null)
                    onProgressUpdate(4) // dismiss showing message
                    callback.sCallback()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}