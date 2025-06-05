package com.smile.colorballs.views.compose

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.interfaces.PresentViewCompose
import com.smile.colorballs.presenters.PresenterCompose
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.viewmodel.MainViewModel
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.FontAndBitmapUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

abstract class MyViewCompose: ComponentActivity(), PresentViewCompose {

    companion object {
        private const val TAG = "MyViewCompose"
    }

    abstract fun showInterstitialAd()
    abstract fun quitOrNewGame()
    abstract fun setDialogStyle(dialog: DialogInterface)

    protected val viewModel: MainViewModel by viewModels()

    protected var textFontSize = 0f
    protected var interstitialAd: ShowInterstitial? = null
    protected lateinit var mPresenter: PresenterCompose

    protected var mImageSizeDp = 0f
    protected var boxImage: Bitmap? = null
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorNextBallMap: HashMap<Int, Bitmap> = HashMap()

    private lateinit var medalImageIds: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$TAG.onCreate")

        Log.d(TAG, "onCreate.textFontSize")
        textFontSize = ScreenUtil.suitableFontSize(
            this, ScreenUtil.getDefaultTextSizeFromTheme(this,
                ScreenUtil.FontSize_Pixel_Type, null),
            ScreenUtil.FontSize_Pixel_Type,
            0.0f)
        Composables.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp

        Log.d(TAG, "onCreate.interstitialAd")
        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, it.facebookAds,
                it.googleInterstitialAd)
        }

        medalImageIds = listOf(
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

        viewModel.medalImageIds = medalImageIds
        Log.d(TAG, "onCreate.instantiate PresenterCompose")
        mPresenter = PresenterCompose(this@MyViewCompose)
    }

    protected fun bitmapDrawableResources() {
        val imageSizePx = ScreenUtil.dpToPixel(mImageSizeDp)
        Log.d(TAG, "bitmapDrawableResources.mImageSizeDp = $mImageSizeDp")
        Log.w(TAG, "bitmapDrawableResources.imageSizePx = $imageSizePx")
        val ballWidth = imageSizePx.toInt()
        val ballHeight = imageSizePx.toInt()
        val nextBallWidth = (imageSizePx * 0.5f).toInt()
        val nextBallHeight = (imageSizePx * 0.5f).toInt()
        val ovalBallWidth = (imageSizePx * 0.9f).toInt()
        val ovalBallHeight = (imageSizePx * 0.7f).toInt()

        BitmapFactory.decodeResource(resources, R.drawable.box_image).let { bm ->
            boxImage = createScaledBitmap(bm, ballWidth, ballHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.redball)?.let { bm ->
            colorBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_RED] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.greenball)?.let { bm ->
            colorBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_GREEN] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.blueball)?.let { bm ->
            colorBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_BLUE] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.magentaball)?.let { bm ->
            colorBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_MAGENTA] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.yellowball)?.let { bm ->
            colorBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_YELLOW] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }

        BitmapFactory.decodeResource(resources, R.drawable.cyanball)?.let { bm ->
            colorBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, ballWidth, ballHeight, true)
            colorNextBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, nextBallWidth, nextBallHeight, true)
            colorOvalBallMap[Constants.COLOR_CYAN] =
                createScaledBitmap(bm, ovalBallWidth, ovalBallHeight, true)
        }
    }

    @Composable
    protected fun SaveGameDialog() {
        Log.d(TAG, "SaveGameDialog")
        val dialogText = mPresenter.saveGameText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener<Unit> {
                override fun buttonOkClick(passedValue: Unit?) {
                    val numOfSaved: Int = mPresenter.readNumberOfSaved()
                    val msg = if (mPresenter.startSavingGame(numOfSaved)) {
                            getString(R.string.succeededSaveGameStr)
                        } else {
                            getString(R.string.failedSaveGameStr)
                        }
                    ScreenUtil.showToast(this@MyViewCompose, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    mPresenter.setShowingSureSaveDialog(false)
                    mPresenter.setSaveGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick(passedValue: Unit?) {
                    mPresenter.setShowingSureSaveDialog(false)
                    mPresenter.setSaveGameText("")
                }
            }
            Composables.DialogWithText(this@MyViewCompose,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    protected fun LoadGameDialog() {
        Log.d(TAG, "LoadGameDialog")
        val dialogText = mPresenter.loadGameText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener<Unit> {
                override fun buttonOkClick(passedValue: Unit?) {
                    val msg = if (mPresenter.startLoadingGame()) {
                        getString(R.string.succeededLoadGameStr)
                    } else {
                        getString(R.string.failedLoadGameStr)
                    }
                    ScreenUtil.showToast(this@MyViewCompose, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    mPresenter.setShowingSureLoadDialog(false)
                    mPresenter.setLoadGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick(passedValue: Unit?) {
                    mPresenter.setShowingSureLoadDialog(false)
                    mPresenter.setLoadGameText("")
                }
            }
            Composables.DialogWithText(this@MyViewCompose,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    protected fun GameOverDialog() {
        Log.d(TAG, "GameOverDialog")
        val dialogText = mPresenter.gameOverText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener<Unit> {
                override fun buttonOkClick(passedVlaue: Unit?) {
                    mPresenter.newGame()
                    mPresenter.setShowingGameOverDialog(false)
                    mPresenter.setGameOverText("")
                }
                override fun buttonCancelClick(passedVlaue: Unit?) {
                    mPresenter.quitGame()
                    mPresenter.setShowingGameOverDialog(false)
                    mPresenter.setGameOverText("")
                }
            }
            Composables.DialogWithText(this@MyViewCompose,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    protected fun SaveScoreDialog() {
        Log.d(TAG, "SaveScoreDialog")
        val dialogTitle = mPresenter.saveScoreTitle.value
        if (dialogTitle.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener<String> {
                override fun buttonOkClick(passedValue: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonOkClick.userName = $passedValue")
                    mPresenter.saveScore(passedValue ?: "No Name")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
                    mPresenter.setSaveScoreTitle("")
                }
                override fun buttonCancelClick(passedValue: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonCancelClick.userName = $passedValue")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
                    mPresenter.setSaveScoreTitle("")
                }
            }
            val hitStr = getString(R.string.nameStr)
            Composables.DialogWithTextField(this@MyViewCompose,
                buttonListener, dialogTitle, hitStr)
        }
    }

    // implementing PresentView
    override fun soundPool(): SoundPoolUtil {
        return SoundPoolUtil(this, R.raw.uhoh)
    }

    override fun getHighestScore() : Int {
        Log.d(TAG, "getHighestScore")
        val scoreSQLiteDB = ScoreSQLite(this)
        val score = scoreSQLiteDB.readHighestScore()
        scoreSQLiteDB.close()
        return score
    }

    override fun addScoreInLocalTop10(playerName : String, score : Int) {
        val scoreSQLiteDB = ScoreSQLite(this)
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

    private fun bitmapToDrawable(bm : Bitmap, width : Int, height : Int) : Drawable? {
        return FontAndBitmapUtil.convertBitmapToDrawable(this, bm,
            width, height)
    }

    override fun showLoadingStrOnScreen() {
        mPresenter.screenMessage.value = getString(R.string.loadingStr)
    }

    override fun showSavingGameStrOnScreen() {
        mPresenter.screenMessage.value = getString(R.string.savingGameStr)
    }

    override fun showLoadingGameStrOnScreen() {
        mPresenter.screenMessage.value = getString(R.string.loadingGameStr)
    }

    override fun showSaveGameDialog() {
        Log.d(TAG, "showSaveGameDialog")
        mPresenter.setSaveGameText(getString(R.string.sureToSaveGameStr))
    }

    override fun showLoadGameDialog() {
        Log.d(TAG, "showLoadGameDialog")
        mPresenter.setLoadGameText(getString(R.string.sureToLoadGameStr))
    }

    override fun showGameOverDialog() {
        mPresenter.setGameOverText(getString(R.string.gameOverStr))
    }

    override fun showSaveScoreAlertDialog() {
        mPresenter.setSaveScoreTitle(getString(R.string.saveScoreStr))
    }
    // end of implementing
}