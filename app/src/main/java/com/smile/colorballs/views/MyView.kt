package com.smile.colorballs.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.interfaces.PresentViewCompose
import com.smile.colorballs.presenters.Presenter
import com.smile.colorballs.viewmodel.ColorBallViewModel
import com.smile.smilelibraries.AdMobBanner
import com.smile.smilelibraries.FacebookBanner
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable
import com.facebook.ads.AdSize

abstract class MyView: ComponentActivity(), PresentViewCompose {

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

    private var interstitialAd: ShowInterstitial? = null

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
        Log.d(TAG, "onCreate.application = $application")
        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, it.facebookAds,
                it.googleInterstitialAd)
        }

        Log.d(TAG, "onCreate.instantiate PresenterCompose")
        viewModel.setPresenter(Presenter(this@MyView))
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
    }

    private fun exitApplication() {
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 200
        // exit application
        handlerClose.postDelayed({ this.finish() }, timeDelay.toLong())
    }

    private fun quitOrNewGame() {
        if (viewModel.mGameAction == Constants.IS_QUITING_GAME) {
            //  END PROGRAM
            exitApplication()
        } else if (viewModel.mGameAction == Constants.IS_CREATING_GAME) {
            //  NEW GAME
            viewModel.initGame(null)
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

    @Composable
    fun ShowAdmobBanner(modifier: Modifier = Modifier,
                                adId: String, width: Int = 0) {
        Log.d(TAG, "ShowAdmobBanner.adId = $adId")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                AdView(context)
            },
            update = { adView ->
                AdMobBanner(adView, adId, width)
            }
        )
    }

    @Composable
    fun ShowAdmobNormalBanner(modifier: Modifier = Modifier) {
        val adId = ColorBallsApp.googleAdMobBannerID
        Log.d(TAG, "ShowAdmobNormalBanner.adId = $adId")
        ShowAdmobBanner(modifier = modifier, adId = adId)
    }

    @Composable
    fun ShowAdmobAdaptiveBanner(modifier: Modifier = Modifier, width: Int) {
        val adId = ColorBallsApp.googleAdMobBannerID2
        Log.d(TAG, "ShowAdmobAdaptiveBanner.adId = $adId")
        ShowAdmobBanner(modifier = modifier, adId = adId, width = width)
    }

    @Composable
    fun MyNativeAdView(
        modifier: Modifier = Modifier,
        ad: NativeAd,
        adContent: @Composable (ad: NativeAd, view: View) -> Unit,
    ) {
        Log.d(TAG, "MyNativeAdView")
        val contentViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        val adViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        Log.d(TAG, "MyNativeAdView.AndroidView")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                Log.d(TAG, "MyNativeAdView.AndroidView.factory")
                val contentView = ComposeView(context).apply {
                    id = contentViewId
                }
                NativeAdView(context).apply {
                    id = adViewId
                    addView(contentView)
                }
            },
            update = { nativeAdView ->
                Log.d(TAG, "MyNativeAdView.AndroidView.update")
                val adView = nativeAdView.findViewById<NativeAdView>(adViewId)
                val contentView = nativeAdView.findViewById<ComposeView>(contentViewId)
                Log.d(TAG, "MyNativeAdView.AndroidView.update.setNativeAd()")
                adView.setNativeAd(ad)
                adView.background = (-0x1).toDrawable()
                adView.callToActionView = contentView
                contentView.setContent { adContent(ad, contentView) }
            }
        )
    }

    @Composable
    fun ShowFacebookBanner(modifier: Modifier = Modifier, adId: String) {
        // val adId = ColorBallsApp.facebookBannerID
        val pId = if (BuildConfig.DEBUG) "IMG_16_9_APP_INSTALL#$adId" else adId
        Log.d(TAG, "ShowFacebookBanner.adId = $adId")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                com.facebook.ads.AdView(context, pId,
                    AdSize.BANNER_HEIGHT_50)
            },
            update = { faceAdView ->
                FacebookBanner(faceAdView)
            }
        )
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
    // end of implementing
}