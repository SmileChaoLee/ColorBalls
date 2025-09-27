package com.smile.colorballs.views

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
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.interfaces.PresentView
import com.smile.colorballs.presenters.Presenter
import com.smile.colorballs.viewmodel.ColorBallViewModel
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.core.graphics.scale
import com.smile.colorballs.constants.WhichGame
import com.smile.colorballs.roomdatabase.ScoreDatabase
import com.smile.smilelibraries.interfaces.DismissFunction

abstract class MyView: ComponentActivity(), PresentView {

    companion object {
        private const val TAG = "MyView"
    }
    protected val viewModel: ColorBallViewModel by viewModels()
    protected var textFontSize = 0f
    protected var mImageSizeDp = 0f
    protected var boxImage: Bitmap? = null
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorNextBallMap: HashMap<Int, Bitmap> = HashMap()
    /**
     * whichGame = 0 : Empty distribution
     * whichGame = 1 : Random distribution
     */
    protected var whichGame = WhichGame.NO_BARRIER

    private var interstitialAd: ShowInterstitial? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "$TAG.onCreate")
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            requestedOrientation = if (ScreenUtil.isTablet(this@MyView)) {
                // Table then change orientation to Landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                // phone then change orientation to Portrait
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        Log.d(TAG, "onCreate.textFontSize")
        textFontSize = ColorBallsApp.textFontSize
        Composables.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp

        Log.d(TAG, "onCreate.interstitialAd")
        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, it.facebookAds,
                it.googleInterstitialAd)
        }

        Log.d(TAG, "onCreate.instantiate PresenterCompose")
        viewModel.setPresenter(Presenter(this@MyView))
        if (savedInstanceState != null) {
            // recreated
            Log.d(TAG, "onCreate.recreated")
            whichGame = viewModel.getWhichGame()
        } else {
            viewModel.setWhichGame(whichGame)
        }
        Log.d(TAG, "onCreate.whichGame = $whichGame")
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
        Log.w(TAG, "bitmapDrawableResources.imageSizePx = $sizePx")
        val ballWidth = sizePx.toInt()
        val ballHeight = sizePx.toInt()
        val nextBallWidth = (sizePx * 0.5f).toInt()
        val nextBallHeight = (sizePx * 0.5f).toInt()
        val ovalBallWidth = (sizePx * 0.9f).toInt()
        val ovalBallHeight = (sizePx * 0.7f).toInt()

        BitmapFactory.decodeResource(resources, R.drawable.box_image).let { bm ->
            boxImage = bm.scale(ballWidth, ballHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.redball)?.let { bm ->
            colorBallMap[Constants.COLOR_RED] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_RED] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_RED] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.greenball)?.let { bm ->
            colorBallMap[Constants.COLOR_GREEN] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_GREEN] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_GREEN] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.blueball)?.let { bm ->
            colorBallMap[Constants.COLOR_BLUE] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_BLUE] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_BLUE] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.magentaball)?.let { bm ->
            colorBallMap[Constants.COLOR_MAGENTA] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_MAGENTA] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_MAGENTA] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.yellowball)?.let { bm ->
            colorBallMap[Constants.COLOR_YELLOW] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_YELLOW] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_YELLOW] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.cyanball)?.let { bm ->
            colorBallMap[Constants.COLOR_CYAN] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_CYAN] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_CYAN] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }

        BitmapFactory.decodeResource(resources, R.drawable.barrier)?.let { bm ->
            colorBallMap[Constants.COLOR_BARRIER] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_BARRIER] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_BARRIER] =
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
        if (viewModel.mGameAction == Constants.IS_QUITING_GAME) {
            //  END PROGRAM
            exitApplication()
        } else if (viewModel.mGameAction == Constants.IS_CREATING_GAME) {
            //  NEW GAME
            interstitialAd?.apply {
                ShowAdThread(object : DismissFunction {
                    override fun backgroundWork() {
                        Log.d(TAG, "quitOrNewGame.backgroundWork")
                    }
                    override fun executeDismiss() {
                        Log.d(TAG, "quitOrNewGame.executeDismiss")
                        viewModel.initGame(null)
                    }
                    override fun afterFinished(isAdShown: Boolean) {
                        Log.d(TAG, "quitOrNewGame.afterFinished.isAdShown= $isAdShown")
                        if (!isAdShown) viewModel.initGame(null)
                    }
                }).startShowAd(0)
            }
        }
        viewModel.setSaveScoreAlertDialogState(false)
        ColorBallsApp.isProcessingJob = false
    }

    @Composable
    fun SaveGameDialog() {
        Log.d(TAG, "SaveGameDialog")
        val dialogText = viewModel.getSaveGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    val numOfSaved: Int = viewModel.readNumberOfSaved()
                    val msg = if (viewModel.startSavingGame(numOfSaved)) {
                            getString(R.string.succeededSaveGameStr)
                        } else {
                            getString(R.string.failedSaveGameStr)
                        }
                    ScreenUtil.showToast(this@MyView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    viewModel.setShowingSureSaveDialog(false)
                    viewModel.setSaveGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setShowingSureSaveDialog(false)
                    viewModel.setSaveGameText("")
                }
            }
            Composables.DialogWithText(this@MyView,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    fun LoadGameDialog() {
        Log.d(TAG, "LoadGameDialog")
        val dialogText = viewModel.getLoadGameText()
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (viewModel.startLoadingGame()) {
                        getString(R.string.succeededLoadGameStr)
                    } else {
                        getString(R.string.failedLoadGameStr)
                    }
                    ScreenUtil.showToast(this@MyView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    viewModel.setShowingSureLoadDialog(false)
                    viewModel.setLoadGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    viewModel.setShowingSureLoadDialog(false)
                    viewModel.setLoadGameText("")
                }
            }
            Composables.DialogWithText(this@MyView,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    fun SaveScoreDialog() {
        Log.d(TAG, "SaveScoreDialog")
        val dialogTitle = viewModel.getSaveScoreTitle()
        if (dialogTitle.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListenerString {
                override fun buttonOkClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonOkClick.value = $value")
                    viewModel.saveScore(value ?: "No Name")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
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
            Composables.DialogWithTextField(this@MyView,
                buttonListener, dialogTitle, hitStr)
        }
    }

    // implementing PresentViewCompose
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
            R.drawable.olympics_image)
        return medalImageIds
    }

    override fun getLoadingStr() = getString(R.string.loadingStr)

    override fun geSavingGameStr() = getString(R.string.savingGameStr)

    override fun getLoadingGameStr() = getString(R.string.loadingGameStr)

    override fun getSureToSaveGameStr() = getString(R.string.sureToSaveGameStr)

    override fun getSureToLoadGameStr() = getString(R.string.sureToLoadGameStr)

    override fun getGameOverStr() = getString(R.string.gameOverStr)

    override fun getSaveScoreStr() = getString(R.string.saveScoreStr)

    override fun soundPool(): SoundPoolUtil {
        return SoundPoolUtil(this, R.raw.uhoh)
    }

    override fun getRoomDatabase(): ScoreDatabase {
        return ScoreDatabase.getDatabase(this,
            viewModel.getDatabaseName())
    }

    override fun fileInputStream(fileName : String): FileInputStream {
        return FileInputStream(File(filesDir, fileName))
    }

    override fun fileOutputStream(fileName : String): FileOutputStream {
        return FileOutputStream(File(filesDir, fileName))
    }
    // end of implementing
}