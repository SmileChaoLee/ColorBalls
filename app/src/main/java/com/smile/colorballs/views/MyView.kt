package com.smile.colorballs.views

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.R
import com.smile.colorballs.ColorBallsApp
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.core.graphics.scale
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.gms.ads.nativead.NativeAd
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.colorballs.interfaces.BasePresentView
import com.smile.colorballs.interfaces.GameOptions
import com.smile.colorballs.presenters.BasePresenter
import com.smile.colorballs.roomdatabase.ScoreDatabase
import com.smile.colorballs.tools.Utils
import com.smile.colorballs.viewmodel.BaseViewModel
import com.smile.colorballs.views.ui.theme.ColorBallsTheme
import com.smile.colorballs.views.ui.theme.ColorPrimary
import com.smile.colorballs.views.ui.theme.Yellow3
import com.smile.smilelibraries.GoogleNativeAd
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class MyView: ComponentActivity(), BasePresentView, GameOptions {

    @Composable
    abstract fun CreateNewGameDialog()
    abstract fun getBasePresenter(): BasePresenter?
    abstract fun getBaseViewModel(): BaseViewModel?
    abstract fun setHasNextForView(hasNext: Boolean)
    abstract fun ifInterstitialWhenSaveScore()
    abstract fun ifInterstitialWhenNewGame()
    abstract fun ifCreatingNewGame(newEasyLevel: Boolean, originalLevel: Boolean)

    protected val menuBarWeight = 1.0f
    protected val gameGridWeight = 7.0f
    protected val mOrientation = mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT)
    protected var screenX = 0f
    protected var screenY = 0f
    protected var textFontSize = 0f
    protected var boxImage: Bitmap? = null
    protected var mImageSizeDp = 0f
    protected val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    protected val colorNextBallMap: HashMap<Int, Bitmap> = HashMap()
    protected var interstitialAd: ShowInterstitial? = null

    private lateinit var basePresenter: BasePresenter
    private lateinit var baseViewModel: BaseViewModel
    private lateinit var mGameOptions: GameOptions

    // the following are for Top 10 Players
    // the following are for Settings
    private lateinit var settingLauncher: ActivityResultLauncher<Intent>
    private lateinit var top10Launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "$TAG.onCreate")
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate.getScreenSize()")
        getScreenSize()

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
        CbComposable.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp

        Log.d(TAG, "onCreate.interstitialAd")
        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, null,
                it.googleInterstitialAd)
        }
        getBasePresenter()?.let {
            basePresenter = it
        } ?: run {
            Log.d(TAG, "onCreate.basePresenter is null so exit activity.")
            return
        }
        getBaseViewModel()?.let {
            baseViewModel = it
        } ?: run {
            Log.d(TAG, "onCreate.baseViewModel is null so exit activity.")
            return
        }
        // The following statements must be after having basePresenter and baseViewModel
        mGameOptions = this as GameOptions
        mGameOptions.setWhichGame()

        top10Launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "top10Launcher.result received")
        }

        settingLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "settingLauncher.result received")
            if (result.resultCode == RESULT_OK) {
                val originalLevel = baseViewModel.isEasyLevel()
                var newEasyLevel: Boolean
                val data = result.data ?: return@registerForActivityResult
                data.extras?.let { extras ->
                    baseViewModel.setHasSound(extras.getBoolean(Constants.HAS_SOUND,
                        true))
                    newEasyLevel = extras.getBoolean(Constants.EASY_LEVEL,
                        originalLevel)
                    baseViewModel.setEasyLevel(newEasyLevel)
                    val hasNext = extras.getBoolean(Constants.HAS_NEXT,true)
                    setHasNextForView(hasNext)
                    ifCreatingNewGame(newEasyLevel, originalLevel)
                }
            }
        }
        enableEdgeToEdge()
        // WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                Scaffold {innerPadding ->
                    mOrientation.intValue = resources.configuration.orientation
                    val backgroundColor = Yellow3
                    Box(Modifier.padding(innerPadding)
                        .background(color = backgroundColor)) {
                        if (mOrientation.intValue ==
                            Configuration.ORIENTATION_PORTRAIT
                        ) {
                            Column {
                                GameView(Modifier.weight(gameGridWeight))
                                SHowPortraitAds(
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(10.0f - menuBarWeight - gameGridWeight)
                                )
                            }
                        } else {
                            Row {
                                GameView(modifier = Modifier.weight(1f))
                                ShowLandscapeAds(modifier = Modifier.weight(1f))
                            }
                        }
                        Box {
                            CreateNewGameDialog()
                            SaveGameDialog()
                            LoadGameDialog()
                            SaveScoreDialog()
                        }
                    }
                }
            }
            LaunchedEffect(Unit) {
                Log.d(TAG, "onCreate.setContent.LaunchedEffect")
                baseViewModel.initGame(savedInstanceState)
            }
        }

        onBackPressedDispatcher.addCallback(object
            : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                onBackWasPressed()
            }
        })
    }

    // should be private after UI migration
    fun onBackWasPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            baseViewModel.quitGame() //   from   END PROGRAM
        } else {
            exitAppTimer.start()
            val toastFontSize = textFontSize * 0.7f
            Log.d(TAG, "toastFontSize = $toastFontSize")
            ScreenUtil.showToast(
                this@MyView,
                getString(R.string.backKeyToExitApp),
                toastFontSize,
                ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun getScreenSize() {
        val screen = ScreenUtil.getScreenSize(this)
        Log.d(TAG, "getScreenSize.screen.x = ${screen.x}")
        Log.d(TAG, "getScreenSize.screen.y = ${screen.y}")
        screenX = screen.x.toFloat()
        screenY = screen.y.toFloat()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged.newConfig.orientation = " +
                "${newConfig.orientation}")
        mOrientation.intValue = newConfig.orientation
        getScreenSize()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        baseViewModel.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        baseViewModel.release()
        interstitialAd?.releaseInterstitial()
    }

    protected fun showInterstitialAd() {
        Log.d(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    protected fun bitmapDrawableResources(sizePx: Float) {
        Log.d(TAG, "bitmapDrawableResources.imageSizePx = $sizePx")
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

    fun quitOrNewGame() {
        if (baseViewModel.mGameAction == Constants.IS_QUITING_GAME) {
            //  END PROGRAM
            exitApplication()
        } else if (baseViewModel.mGameAction == Constants.IS_CREATING_GAME) {
            //  NEW GAME
            ifInterstitialWhenNewGame()
        }
        baseViewModel.setSaveScoreAlertDialogState(false)
        // baseViewModel.isProcessingJob = false
    }

    @Composable
    fun GameView(modifier: Modifier) {
        Log.d(TAG, "GameView.mOrientation.intValue = ${mOrientation.intValue}")
        Log.d(TAG, "GameView.screenX = $screenX, screenY = $screenY")
        var maxWidth = screenX
        // val barWeight = 10.0f - gameGridWeight
        // menuBarWeight = 1.0f
        var gameWeight = gameGridWeight
        if (mOrientation.intValue == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "GameView.ORIENTATION_LANDSCAPE")
            maxWidth = screenX/2.0f
            // menuBarWeight = 1.0f
            gameWeight = 9.0f
        }
        val gridHeight = ScreenUtil.pixelToDp(screenY) * gameWeight / 10.0f
        Log.d(TAG, "GameView.gridHeight = $gridHeight")
        // val heightPerBall = gridHeight / Constants.ROW_COUNTS
        val heightPerBall = gridHeight / baseViewModel.rowCounts
        Log.d(TAG, "GameView.heightPerBall = $heightPerBall")
        // val widthPerBall = ScreenUtil.pixelToDp(maxWidth) / Constants.COLUMN_COUNTS
        val widthPerBall = ScreenUtil.pixelToDp(maxWidth) / baseViewModel.colCounts
        Log.d(TAG, "GameView.widthPerBall = $widthPerBall")
        // set size of color balls
        mImageSizeDp = if (heightPerBall>widthPerBall) widthPerBall
        else heightPerBall
        mImageSizeDp = (mImageSizeDp*100f).toInt().toFloat() / 100f
        Log.d(TAG, "GameView.mImageSizeDp = $mImageSizeDp")
        val sizePx = ScreenUtil.dpToPixel(mImageSizeDp)
        bitmapDrawableResources(sizePx)

        val topPadding = 0f
        // val backgroundColor = Color(getColor(R.color.yellow3))
        Column(modifier = modifier.fillMaxHeight()) {
            ToolBarMenu(modifier = Modifier
                .weight(menuBarWeight)
                .padding(top = topPadding.dp, start = 0.dp))
            Column(modifier = Modifier.weight(gameWeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                GameViewGrid()
            }
        }
    }

    @Composable
    fun ToolBarMenu(modifier: Modifier) {
        Log.d(TAG, "ToolBarMenu.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Row(modifier = modifier
            .background(color = Color(getColor(R.color.colorPrimary)))) {
            ShowCurrentScore(
                Modifier
                    .weight(2f)
                    .padding(start = 10.dp)
                    .align(Alignment.CenterVertically))
            SHowHighestScore(
                Modifier
                    .weight(2f)
                    .align(Alignment.CenterVertically))
            UndoButton(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
            SettingButton(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
            ShowMenu(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
        }
    }

    @Composable
    fun ShowCurrentScore(modifier: Modifier) {
        Log.d(TAG, "ShowCurrentScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = baseViewModel.getCurrentScore().toString(),
            modifier = modifier,
            color = Color.Red, fontSize = CbComposable.mFontSize
        )
    }

    @Composable
    fun SHowHighestScore(modifier: Modifier) {
        Log.d(TAG, "SHowHighestScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = baseViewModel.getHighestScore().toString(),
            modifier = modifier,
            color = Color.White, fontSize = CbComposable.mFontSize
        )
    }

    @Composable
    fun SaveGameDialog() {
        Log.d(TAG, "SaveGameDialog")
        val dialogText = baseViewModel.getSaveGameText()
        if (dialogText.isNotEmpty()) {
            baseViewModel.setShowingSureSaveDialog(true)
            val buttonListener = object: CbComposable.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (baseViewModel.startSavingGame()) {
                            getString(R.string.succeededSaveGameStr)
                        } else {
                            getString(R.string.failedSaveGameStr)
                        }
                    ScreenUtil.showToast(this@MyView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    baseViewModel.setSaveGameText("")
                    baseViewModel.setShowingSureSaveDialog(false)
                    // showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    baseViewModel.setSaveGameText("")
                    baseViewModel.setShowingSureSaveDialog(false)
                }
            }
            CbComposable.DialogWithText(
                this@MyView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun LoadGameDialog() {
        Log.d(TAG, "LoadGameDialog")
        val dialogText = baseViewModel.getLoadGameText()
        if (dialogText.isNotEmpty()) {
            baseViewModel.setShowingSureLoadDialog(true)
            val buttonListener = object: CbComposable.ButtonClickListener {
                override fun buttonOkClick() {
                    val msg = if (baseViewModel.startLoadingGame()) {
                        getString(R.string.succeededLoadGameStr)
                    } else {
                        getString(R.string.failedLoadGameStr)
                    }
                    ScreenUtil.showToast(this@MyView, msg, textFontSize,
                        ScreenUtil.FontSize_Pixel_Type, Toast.LENGTH_LONG)
                    baseViewModel.setLoadGameText("")
                    baseViewModel.setShowingSureLoadDialog(false)
                    // showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    baseViewModel.setLoadGameText("")
                    baseViewModel.setShowingSureLoadDialog(false)
                }
            }
            CbComposable.DialogWithText(
                this@MyView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun SaveScoreDialog() {
        Log.d(TAG, "SaveScoreDialog")
        val dialogTitle = baseViewModel.getSaveScoreTitle()
        if (dialogTitle.isNotEmpty()) {
            baseViewModel.setSaveScoreAlertDialogState(true)
            val buttonListener = object: CbComposable.ButtonClickListenerString {
                override fun buttonOkClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonOkClick.value = $value")
                    baseViewModel.saveScore(value ?: "No Name")
                    baseViewModel.setSaveScoreTitle("")
                    quitOrNewGame()
                }
                override fun buttonCancelClick(value: String?) {
                    Log.d(TAG, "SaveScoreDialog.buttonCancelClick.value = $value")
                    // set SaveScoreDialog() invisible
                    baseViewModel.setSaveScoreTitle("")
                    quitOrNewGame()
                }
            }
            val hitStr = getString(R.string.nameStr)
            CbComposable.DialogWithTextField(
                this@MyView,
                buttonListener, dialogTitle, hitStr
            )
        } else {
            ifInterstitialWhenSaveScore()
        }
    }

    @Composable
    fun ShowMessageOnScreen() {
        Log.d(TAG, "ShowMessageOnScreen.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val message = baseViewModel.getScreenMessage()
        Log.d(TAG, "ShowMessageOnScreen.message = $message")
        if (message.isEmpty()) return
        baseViewModel.setShowingMessageDialog(true)
        val gameViewLength = mImageSizeDp * baseViewModel.colCounts.toFloat()
        var width = (gameViewLength/2f).dp
        val widthStr = (message.length * ScreenUtil.pixelToDp(textFontSize)).dp
        if (widthStr > width ) width = widthStr
        val height = (gameViewLength/4f).dp
        val modifier = Modifier.background(color = Color.Transparent)
        Column(modifier = modifier) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.dialog_board_image),
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(width = width, height = height)
                )
                Text(
                    modifier = Modifier.align(alignment = Alignment.Center),
                    text = message,
                    color = Color.Red, fontSize = textFontSize.sp
                )
            }
        }
        baseViewModel.setShowingMessageDialog(false)
    }

    @Composable
    fun SHowPortraitAds(modifier: Modifier) {
        Log.d(TAG, "SHowPortraitAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        /*
        val adWidth = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width
                .toDp().value.toInt()
        }
        */
        Column(modifier = modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            // CbComposable.ShowAdmobNormalBanner(modifier = Modifier.weight(1.0f))
            CbComposable.ShowAdmobAdaptiveBanner(modifier = Modifier.weight(1.0f), 0)
            CbComposable.ShowFacebookBanner(modifier = Modifier.weight(1.0f),
                ColorBallsApp.facebookBannerID)
            // CbComposable.ShowFacebookBanner(modifier = Modifier.weight(1.0f),
            //     ColorBallsApp.facebookBannerID2)
        }
    }

    @Composable
    fun ShowNativeAd(modifier: Modifier = Modifier) {
        Log.d(TAG, "ShowNativeAd.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
        LaunchedEffect(Unit) {
            object : GoogleNativeAd(this@MyView,
                ColorBallsApp.googleAdMobNativeID) {
                override fun setNativeAd(ad: NativeAd?) {
                    Log.d(TAG, "ShowNativeAd.GoogleNativeAd.setNativeAd")
                    nativeAd = ad
                }
            }
        }   // end of LaunchedEffect
        nativeAd?.let {
            CbComposable.MyNativeAdView(modifier = modifier, ad = it) { ad, view ->
                // head Column
                Column(modifier = modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    // head Row
                    Row(
                        modifier = Modifier
                            .weight(8.0f)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val icon: Drawable? = ad.icon?.drawable
                        icon?.let { drawable ->
                            Image(
                                painter = rememberDrawablePainter(drawable = drawable),
                                contentDescription = ""/*ad.icon?.contentDescription*/,
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column {
                            Text(
                                text = ad.headline ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = ad.body ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }   // end of Column
                    }   // end of head Row
                    // Column for Button
                    ad.callToAction?.let { cta ->
                        Log.d(TAG, "ShowNativeAd.callToAction.cta = $cta")
                        Column(modifier = Modifier
                            .weight(2.0f)
                            .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = {
                                view.performClick()
                            }, colors = ButtonColors(
                                containerColor = ColorPrimary,
                                disabledContainerColor = ColorPrimary,
                                contentColor = Color.Yellow,
                                disabledContentColor = Color.Yellow)
                            ) { Text(text = cta, fontSize = CbComposable.mFontSize) }
                        }
                    }   // end of ad.callToAction */
                }   // end of head Column
            }   // end of MyNativeAdView
        }
    }

    @Composable
    fun ShowLandscapeAds(modifier: Modifier) {
        Log.d(TAG, "ShowLandscapeAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val colHeight = with(LocalDensity.current) {
            screenY.toDp()
        }
        Column(modifier = modifier
            .height(height = colHeight)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            ShowNativeAd(modifier = Modifier.weight(8.0f))
            // CbComposable.ShowAdmobNormalBanner(modifier = Modifier.weight(2.0f))
            CbComposable.ShowAdmobAdaptiveBanner(modifier = Modifier.weight(2.0f), 0)
            /*
            Composables.ShowFacebookBanner(modifier = Modifier.weight(2.0f)
                .padding(top = 10.dp),
                ColorBallsApp.facebookBannerID2)
            */
        }
    }

    private fun showColorWhenClick(isClicked: MutableState<Boolean>) {
        CoroutineScope(Dispatchers.Default).launch {
            isClicked.value = true
            delay(500)
            isClicked.value = false
        }
    }

    private fun showTop10Players(isLocal: Boolean) {
        Log.d(TAG, "showTop10Players.isLocal = $isLocal")
        Intent(
            this@MyView,
            Top10Activity::class.java
        ).let {
            Bundle().apply {
                putString(Constants.GAME_ID, Utils.getGameId(baseViewModel.getWhichGame()))
                putString(Constants.DATABASE_NAME, Utils.getDatabaseName(baseViewModel.getWhichGame()))
                putBoolean(Constants.IS_LOCAL_TOP10, isLocal)
                it.putExtras(this)
                top10Launcher.launch(it)
            }
        }
    }

    private fun onClickSettingButton() {
        if (baseViewModel.isProcessingJob()) return
        Intent(
            this@MyView,
            CbSettingActivity::class.java
        ).let {
            Bundle().apply {
                putString(Constants.GAME_ID, Utils.getGameId(baseViewModel.getWhichGame()))
                putBoolean(Constants.HAS_SOUND, baseViewModel.hasSound())
                Log.d(TAG, "onClickSettingButton.baseViewModel.isEasyLevel() = ${baseViewModel.isEasyLevel()}")
                putBoolean(Constants.EASY_LEVEL, baseViewModel.isEasyLevel())
                putBoolean(Constants.HAS_NEXT, baseViewModel.hasNext())
                it.putExtras(this)
                settingLauncher.launch(it)
            }
        }
    }

    @Composable
    fun UndoButton(modifier: Modifier) {
        Log.d(TAG, "UndoButton.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val isClicked = remember { mutableStateOf(false) }
        IconButton (onClick = {
            showColorWhenClick(isClicked)
            baseViewModel.undoTheLast()
        },
            modifier = modifier
            /*, colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent) */ ) {
            Icon(
                painter = painterResource(R.drawable.undo),
                contentDescription = "",
                tint = if (isClicked.value) Color.Red else Color.White
            )
        }
    }

    @Composable
    fun SettingButton(modifier: Modifier) {
        Log.d(TAG, "SettingButton.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val isClicked = remember { mutableStateOf(false) }
        IconButton (onClick = {
            showColorWhenClick(isClicked)
            onClickSettingButton()
        },
            modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.setting),
                contentDescription = "",
                tint = if (isClicked.value) Color.Red else Color.White
            )
        }
    }

    @Composable
    fun ShowMenu(modifier: Modifier) {
        Log.d(TAG, "ShowMenu.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val dropdownWidth =
            if (mOrientation.intValue == Configuration.ORIENTATION_PORTRAIT) {
                mImageSizeDp * 6.0f
            } else {
                mImageSizeDp * 8.0f
            }
        var expanded by remember { mutableStateOf(false) }
        Log.d(TAG, "ShowMenu.expanded = $expanded")
        Column(modifier = modifier) {
            IconButton (onClick = {
                if (!baseViewModel.isProcessingJob()) {
                    expanded = !expanded
                }
            }, modifier = modifier) {
                Icon(
                    painter = painterResource(R.drawable.three_dots),
                    contentDescription = "",
                    tint = if (expanded) Color.Red else Color.White
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .requiredHeightIn(max = (mImageSizeDp * 12f).dp)
                    .requiredWidth(dropdownWidth.dp)
                    .background(
                        color =
                            Color(getColor(android.R.color.holo_green_light))
                    )
                    .padding(all = 0.dp),
            ) {
                val isGlobalTop10Clicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.globalTop10Str),
                    color = if (isGlobalTop10Clicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isGlobalTop10Clicked)
                        showTop10Players(isLocal = false)
                    })

                val isLocalTop10Clicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.localTop10Score),
                    color = if (isLocalTop10Clicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isLocalTop10Clicked)
                        showTop10Players(isLocal = true)
                    })

                val isSaveGameClicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.saveGameStr),
                    color = if (isSaveGameClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isSaveGameClicked)
                        baseViewModel.saveGame()
                    })

                val isLoadGameClicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.loadGameStr),
                    color = if (isLoadGameClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isLoadGameClicked)
                        baseViewModel.loadGame()
                    })

                val isNewGameClicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.newGame),
                    color = if (isNewGameClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isNewGameClicked)
                        baseViewModel.newGame()
                    })

                val isPrivacyClicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.privacyPolicyString),
                    color = if (isPrivacyClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isPrivacyClicked)
                        PrivacyPolicyUtil.startPrivacyPolicyActivity(
                            this@MyView, 10)
                    },
                    isDivider = false)
            }
        }
    }

    @Composable
    fun GameViewGrid(modifier: Modifier = Modifier) {
        Log.d(TAG, "GameViewGrid.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center) {
                ShowGameGrid()
                ShowMessageOnScreen()
            }
        }
    }

    @Composable
    fun ShowGameGrid() {
        Log.d(TAG, "ShowGameGrid.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Log.d(TAG, "ShowGameGrid.mImageSizeDp = $mImageSizeDp")
        Column {
            for (i in 0 until baseViewModel.rowCounts) {
                Row {
                    for (j in 0 until baseViewModel.colCounts) {
                        Box(Modifier
                            .clickable {
                                baseViewModel.cellClickListener(i, j)
                            }) {
                            Image(
                                modifier = Modifier
                                    .size(mImageSizeDp.dp)
                                    .padding(all = 0.dp),
                                // painter = painterResource(id = R.drawable.box_image),
                                // painter = rememberDrawablePainter(drawable = boxImage),
                                // bitmap = boxImage!!.asImageBitmap(),
                                painter = BitmapPainter(boxImage!!.asImageBitmap()),
                                contentDescription = "",
                                contentScale = ContentScale.FillBounds
                            )
                            ShowColorBall(i, j)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ShowColorBall(i: Int, j: Int) {
        Log.d(TAG, "ShowColorBall.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val ballInfo = baseViewModel.gridDataArray[i][j].value
        val ballColor = ballInfo.ballColor
        Log.d(TAG, "ShowColorBall.ballColor = $ballColor")
        val isAnimation = ballInfo.isAnimation
        Log.d(TAG, "ShowColorBall.isAnimation = $isAnimation")
        val isReSize = ballInfo.isResize
        Log.d(TAG, "ShowColorBall.isReSize = $isReSize")
        if (ballColor == 0) return  // no showing ball
        val bitmap: Bitmap? = when(ballInfo.whichBall) {
            WhichBall.BALL-> { colorBallMap.getValue(ballColor) }
            WhichBall.OVAL_BALL-> { colorOvalBallMap.getValue(ballColor) }
            // BallsRemover does not implement  Next Ball, so it should be OK
            WhichBall.NEXT_BALL-> { colorNextBallMap.getValue(ballColor) }
            WhichBall.NO_BALL -> { null }
        }
        Column(modifier = Modifier.size(mImageSizeDp.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            var modifier = Modifier.background(color = Color.Transparent)
            var scale: ContentScale = ContentScale.Inside
            if (isReSize) {
                modifier = modifier
                    .size(mImageSizeDp.dp)
                    .padding(all = 0.dp)
                scale = ContentScale.Fit
            }
            Image(
                painter = BitmapPainter(bitmap!!.asImageBitmap()),
                contentDescription = "",
                contentScale = scale,
                modifier = modifier
            )
        }
    }

    // implementing BasePresentView
    override fun getLoadingStr() = getString(R.string.loadingStr)

    override fun geSavingGameStr() = getString(R.string.savingGameStr)

    override fun getLoadingGameStr() = getString(R.string.loadingGameStr)

    override fun getSureToSaveGameStr() = getString(R.string.sureToSaveGameStr)

    override fun getSureToLoadGameStr() = getString(R.string.sureToLoadGameStr)

    override fun getSaveScoreStr() = getString(R.string.saveScoreStr)

    override fun soundPool(): SoundPoolUtil {
        return SoundPoolUtil(this, R.raw.uhoh)
    }

    override fun getRoomDatabase(): ScoreDatabase {
        return ScoreDatabase.getDatabase(this,
            Utils.getDatabaseName(baseViewModel.getWhichGame()))
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