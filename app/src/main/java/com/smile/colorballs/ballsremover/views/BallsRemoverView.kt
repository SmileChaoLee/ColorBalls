package com.smile.colorballs.ballsremover.views

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.smile.colorballs.ballsremover.BallsRemoverComposables
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.R
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.ballsremover.constants.BallsRemoverConstants
import com.smile.colorballs.ballsremover.interfaces.BallsRemoverPresentView
import com.smile.colorballs.ballsremover.presenters.BallsRemoverPresenter
import com.smile.colorballs.ballsremover.viewmodels.BallsRemoverViewModel
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.core.graphics.scale

abstract class BallsRemoverView: ComponentActivity(),
    BallsRemoverPresentView {

    protected val viewModel: BallsRemoverViewModel by viewModels()
    protected var textFontSize = 0f
    protected var boxImage: Bitmap? = null
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()

    private val databaseName = "balls_remover.db"
    private var interstitialAd: ShowInterstitial? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "$TAG.onCreate")
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            requestedOrientation = if (ScreenUtil.isTablet(this@BallsRemoverView)) {
                // Table then change orientation to Landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                // phone then change orientation to Portrait
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        Log.d(TAG, "onCreate.textFontSize")
        textFontSize = ColorBallsApp.textFontSize
        BallsRemoverComposables.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp

        Log.d(TAG, "onCreate.interstitialAd")
        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, null,
                it.googleInterstitialAd)
        }

        Log.d(TAG, "onCreate.instantiate BallsRemoverPresenter")
        viewModel.setPresenter(BallsRemoverPresenter(this@BallsRemoverView))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        viewModel.release()
        interstitialAd?.releaseInterstitial()
    }

    protected fun showInterstitialAd() {
        Log.d(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    protected fun bitmapDrawableResources(sizePx: Float) {
        Log.w(TAG, "bitmapDrawableResources.sizePx = $sizePx")
        val ballWidth = sizePx.toInt()
        val ballHeight = sizePx.toInt()
        val ovalBallWidth = (sizePx * 0.9f).toInt()
        val ovalBallHeight = (sizePx * 0.7f).toInt()

        BitmapFactory.decodeResource(resources, R.drawable.box_image).let { bm ->
            // boxImage = createScaledBitmap(bm, ballWidth, ballHeight, true)
            boxImage = bm.scale(ballWidth, ballHeight)
        }
        Log.d(TAG, "bitmapDrawableResources.boxImage.height = ${boxImage?.height}")
        Log.d(TAG, "bitmapDrawableResources.boxImage.height.toDp " +
                "= ${ScreenUtil.pixelToDp(boxImage?.height!!.toFloat())}")

        BitmapFactory.decodeResource(resources, R.drawable.redball)?.let { bm ->
            colorBallMap[BallsRemoverConstants.COLOR_RED] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[BallsRemoverConstants.COLOR_RED] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.greenball)?.let { bm ->
            colorBallMap[BallsRemoverConstants.COLOR_GREEN] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[BallsRemoverConstants.COLOR_GREEN] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.blueball)?.let { bm ->
            colorBallMap[BallsRemoverConstants.COLOR_BLUE] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[BallsRemoverConstants.COLOR_BLUE] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.magentaball)?.let { bm ->
            colorBallMap[BallsRemoverConstants.COLOR_MAGENTA] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[BallsRemoverConstants.COLOR_MAGENTA] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.yellowball)?.let { bm ->
            colorBallMap[BallsRemoverConstants.COLOR_YELLOW] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[BallsRemoverConstants.COLOR_YELLOW] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.cyanball)?.let { bm ->
            colorBallMap[BallsRemoverConstants.COLOR_CYAN] =
                bm.scale(ballWidth, ballHeight)
            colorOvalBallMap[BallsRemoverConstants.COLOR_CYAN] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }
    }

    private fun exitApplication() {
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 1000
        // exit application
        handlerClose.postDelayed({ this.finish() }, timeDelay.toLong())
    }

    private fun quitOrNewGame() {
        if (viewModel.mGameAction == BallsRemoverConstants.IS_QUITING_GAME) {
            //  END PROGRAM
            exitApplication()
        } else if (viewModel.mGameAction == BallsRemoverConstants.IS_CREATING_GAME) {
            //  NEW GAME
            viewModel.initGame(null)
        }
    }

    @Composable
    fun CreateNewGameDialog() {
        Log.d(TAG, "CreateNewGameDialog")
        val dialogText = viewModel.getCreateNewGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: BallsRemoverComposables.ButtonClickListener {
                override fun buttonOkClick() {
                    viewModel.setCreateNewGameText("")
                    quitOrNewGame()
                }
                override fun buttonCancelClick() {
                    viewModel.setCreateNewGameText("")
                }
            }
            BallsRemoverComposables.DialogWithText(
                this@BallsRemoverView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun SaveGameDialog() {
        Log.d(TAG, "SaveGameDialog")
        val dialogText = viewModel.getSaveGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: BallsRemoverComposables.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (viewModel.startSavingGame()) {
                            getString(R.string.succeededSaveGameStr)
                        } else {
                            getString(R.string.failedSaveGameStr)
                        }
                    ScreenUtil.showToast(this@BallsRemoverView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    viewModel.setSaveGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setSaveGameText("")
                }
            }
            BallsRemoverComposables.DialogWithText(
                this@BallsRemoverView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun LoadGameDialog() {
        Log.d(TAG, "LoadGameDialog")
        val dialogText = viewModel.getLoadGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: BallsRemoverComposables.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (viewModel.startLoadingGame()) {
                        getString(R.string.succeededLoadGameStr)
                    } else {
                        getString(R.string.failedLoadGameStr)
                    }
                    ScreenUtil.showToast(this@BallsRemoverView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    viewModel.setLoadGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setLoadGameText("")
                }
            }
            BallsRemoverComposables.DialogWithText(
                this@BallsRemoverView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun SaveScoreDialog() {
        Log.d(TAG, "SaveScoreDialog")
        val dialogTitle = viewModel.getSaveScoreTitle()
        if (dialogTitle.isNotEmpty()) {
            val buttonListener = object: BallsRemoverComposables.ButtonClickListenerString {
                override fun buttonOkClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonOkClick.value = $value")
                    viewModel.saveScore(value ?: "No Name")
                    quitOrNewGame()
                    viewModel.setSaveScoreTitle("")
                }
                override fun buttonCancelClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonCancelClick.value = $value")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
                    viewModel.setSaveScoreTitle("")
                }
            }
            val hitStr = getString(R.string.nameStr)
            BallsRemoverComposables.DialogWithTextField(
                this@BallsRemoverView,
                buttonListener, dialogTitle, hitStr
            )
        } else {
            if (viewModel.timesPlayed >= BallsRemoverConstants.SHOW_ADS_AFTER_TIMES) {
                Log.d(TAG, "SaveScoreDialog.showInterstitialAd")
                showInterstitialAd()
                viewModel.timesPlayed = 0
            }
        }
    }

    // implementing MainPresentView
    override fun getMedalImageIds(): List<Int> {
        val medalImageIds = listOf(
            R.drawable.gold_medal,
            R.drawable.silver_medal,
            R.drawable.bronze_medal,
            R.drawable.copper_medal,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image,
            R.drawable.olympics_image
        )
        return medalImageIds
    }

    override fun getCreateNewGameStr() = getString(R.string.createNewGameStr)

    override fun getLoadingStr() = getString(R.string.loadingStr)

    override fun geSavingGameStr() = getString(R.string.savingGameStr)

    override fun getLoadingGameStr() = getString(R.string.loadingGameStr)

    override fun getSureToSaveGameStr() = getString(R.string.sureToSaveGameStr)

    override fun getSureToLoadGameStr() = getString(R.string.sureToLoadGameStr)

    override fun getSaveScoreStr() = getString(R.string.saveScoreStr)

    override fun soundPool(): SoundPoolUtil {
        return SoundPoolUtil(this, R.raw.uhoh)
    }

    override fun getHighestScore() : Int {
        Log.d(TAG, "getHighestScore")
        val scoreSQLiteDB = ScoreSQLite(this, databaseName)
        val score = scoreSQLiteDB.readHighestScore()
        Log.d(TAG, "getHighestScore.score = $score")
        scoreSQLiteDB.close()
        return score
    }

    override fun addScoreInLocalTop10(playerName : String, score : Int) {
        Log.d(TAG, "addScoreInLocalTop10")
        val scoreSQLiteDB = ScoreSQLite(this, databaseName)
        if (scoreSQLiteDB.isInTop10(score)) {
            // inside top 10, then record the current score
            scoreSQLiteDB.addScore(playerName, score)
            scoreSQLiteDB.deleteAllAfterTop10() // only keep the top 10
        }
        scoreSQLiteDB.close()
    }

    override fun fileInputStream(fileName : String): FileInputStream {
        return FileInputStream(File(filesDir, fileName))
    }

    override fun fileOutputStream(fileName : String): FileOutputStream {
        return FileOutputStream(File(filesDir, fileName))
    }
    // end of implementing


    companion object {
        private const val TAG = "BallsRemoverView"
    }
}