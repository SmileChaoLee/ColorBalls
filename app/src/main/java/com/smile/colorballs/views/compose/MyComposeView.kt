package com.smile.colorballs.views.compose

// For google native ads
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.interfaces.PresentViewCompose
import com.smile.colorballs.presenters.PresenterCompose
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.viewmodel.MainComposeViewModel
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.FontAndBitmapUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

abstract class MyComposeView: ComponentActivity(), PresentViewCompose {

    companion object {
        private const val TAG = "MyComposeView"
    }

    protected val viewModel: MainComposeViewModel by viewModels()
    protected var textFontSize = 0f
    protected lateinit var mPresenter: PresenterCompose
    protected var mImageSizeDp = 0f
    protected var boxImage: Bitmap? = null
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorNextBallMap: HashMap<Int, Bitmap> = HashMap()

    private var interstitialAd: ShowInterstitial? = null
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
        mPresenter = PresenterCompose(this@MyComposeView)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mPresenter.release()
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

    private fun exitApplication() {
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 200
        // exit application
        handlerClose.postDelayed({ this.finish() }, timeDelay.toLong())
    }

    private fun quitOrNewGame() {
        if (mPresenter.mGameAction == Constants.IS_QUITING_GAME) {
            //  END PROGRAM
            exitApplication()
        } else if (mPresenter.mGameAction == Constants.IS_CREATING_GAME) {
            //  NEW GAME
            mPresenter.initGame(null)
        }
        mPresenter.setSaveScoreAlertDialogState(false)
        ColorBallsApp.isProcessingJob = false
    }

    @Composable
    fun SaveGameDialog() {
        Log.d(TAG, "SaveGameDialog")
        val dialogText = mPresenter.saveGameText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    val numOfSaved: Int = mPresenter.readNumberOfSaved()
                    val msg = if (mPresenter.startSavingGame(numOfSaved)) {
                            getString(R.string.succeededSaveGameStr)
                        } else {
                            getString(R.string.failedSaveGameStr)
                        }
                    ScreenUtil.showToast(this@MyComposeView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    mPresenter.setShowingSureSaveDialog(false)
                    mPresenter.setSaveGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    mPresenter.setShowingSureSaveDialog(false)
                    mPresenter.setSaveGameText("")
                }
            }
            Composables.DialogWithText(this@MyComposeView,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    fun LoadGameDialog() {
        Log.d(TAG, "LoadGameDialog")
        val dialogText = mPresenter.loadGameText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (mPresenter.startLoadingGame()) {
                        getString(R.string.succeededLoadGameStr)
                    } else {
                        getString(R.string.failedLoadGameStr)
                    }
                    ScreenUtil.showToast(this@MyComposeView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    mPresenter.setShowingSureLoadDialog(false)
                    mPresenter.setLoadGameText("")
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    mPresenter.setShowingSureLoadDialog(false)
                    mPresenter.setLoadGameText("")
                }
            }
            Composables.DialogWithText(this@MyComposeView,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    fun GameOverDialog() {
        Log.d(TAG, "GameOverDialog")
        val dialogText = mPresenter.gameOverText.value
        if (dialogText.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    mPresenter.newGame()
                    mPresenter.setShowingGameOverDialog(false)
                    mPresenter.setGameOverText("")
                }
                override fun buttonCancelClick() {
                    mPresenter.quitGame()
                    mPresenter.setShowingGameOverDialog(false)
                    mPresenter.setGameOverText("")
                }
            }
            Composables.DialogWithText(this@MyComposeView,
                buttonListener, "", dialogText)
        }
    }

    @Composable
    fun SaveScoreDialog() {
        Log.d(TAG, "SaveScoreDialog")
        val dialogTitle = mPresenter.saveScoreTitle.value
        if (dialogTitle.isNotEmpty()) {
            val buttonListener = object: Composables.ButtonClickListenerString {
                override fun buttonOkClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonOkClick.value = $value")
                    mPresenter.saveScore(value ?: "No Name")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
                    mPresenter.setSaveScoreTitle("")
                }
                override fun buttonCancelClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonCancelClick.value = $value")
                    quitOrNewGame()
                    // set SaveScoreDialog() invisible
                    mPresenter.setSaveScoreTitle("")
                }
            }
            val hitStr = getString(R.string.nameStr)
            Composables.DialogWithTextField(this@MyComposeView,
                buttonListener, dialogTitle, hitStr)
        }
    }

    @Composable
    private fun ShowAdmobBanner(modifier: Modifier = Modifier,
                                adId: String, width: Int = 0) {
        Log.d(TAG, "ShowAdmobBanner.adId = $adId")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                val adView = AdView(context)
                adView.apply {
                    val adMobAdSize = if (width != 0)
                        getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        this@MyComposeView, width) // adaptive banner
                    else AdSize.BANNER // normal banner

                    val listener = object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            Log.d(TAG, "ShowAdmobBanner.failed.adId = $adId")
                        }
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.d(TAG, "ShowAdmobBanner.succeeded.adId = $adId")
                        }
                    }

                    setAdSize(adMobAdSize)
                    adUnitId = adId
                    adListener = listener
                    loadAd(AdRequest.Builder().build())
                }
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
        ad: NativeAd,
        adContent: @Composable (ad: NativeAd, view: View) -> Unit,) {
        Log.d(TAG, "MyNativeAdView")
        val contentViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        val adViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        Log.d(TAG, "MyNativeAdView.AndroidView")
        AndroidView(
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
                adView.background = ColorDrawable(-0x1)
                adView.callToActionView = contentView
                contentView.setContent { adContent(ad, contentView) }
            }
        )
    }

    @Composable
    fun ShowFacebookBanner(modifier: Modifier = Modifier, adId: String) {
        Log.d(TAG, "ShowFacebookBanner.adId = $adId")
        // adId == ColorBallsApp.facebookBannerID)
        // adId == ColorBallsApp.facebookBannerID2)
        AndroidView(
            modifier = modifier,
            factory = { context ->
                val pId = if (BuildConfig.DEBUG) "IMG_16_9_APP_INSTALL#$adId" else adId
                val faceAdview = com.facebook.ads.AdView(context, pId,
                    com.facebook.ads.AdSize.BANNER_HEIGHT_50)
                faceAdview.apply {
                    val listener = object : com.facebook.ads.AdListener {
                        override fun onError(p0: Ad?, p1: AdError?) {
                            Log.d(TAG, "ShowFacebookBanner.onError.adId = $adId")
                        }
                        override fun onAdLoaded(p0: Ad?) {
                            Log.d(TAG, "ShowFacebookBanner.onAdLoaded.adId = $adId")
                        }
                        override fun onAdClicked(p0: Ad?) {
                            Log.d(TAG, "ShowFacebookBanner.onAdClicked.adId = $adId")
                        }
                        override fun onLoggingImpression(p0: Ad?) {
                            Log.d(TAG, "ShowFacebookBanner.onLoggingImpression.adId = $adId")
                        }
                    }
                    Log.d(TAG, "ShowFacebookBanner.loadAd.adId = $adId")
                    loadAd(buildLoadAdConfig()
                        .withAdListener(listener).build())
                }
            }
        )
    }

    // implementing PresentViewCompose
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