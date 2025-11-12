package com.smile.colorballs_main.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs_main.BuildConfig
import com.smile.colorballs_main.R
import com.smile.colorballs_main.BaseApp
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.core.graphics.scale
import androidx.core.view.WindowCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.gms.ads.nativead.NativeAd
import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichBall
import com.smile.colorballs_main.interfaces.BasePresentView
import com.smile.colorballs_main.interfaces.GameOptions
import com.smile.colorballs_main.models.ColorBallInfo
import com.smile.colorballs_main.presenters.BasePresenter
import com.smile.colorballs_main.roomdatabase.ScoreDatabase
import com.smile.colorballs_main.smileapps.SmileAppsActivity
import com.smile.colorballs_main.tools.GameUtil
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.viewmodel.BaseViewModel
import com.smile.colorballs_main.views.ui.theme.ColorBallsTheme
import com.smile.colorballs_main.views.ui.theme.ColorPrimary
import com.smile.colorballs_main.views.ui.theme.Yellow3
import com.smile.smilelibraries.GoogleNativeAd
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseView: ComponentActivity(),
    BasePresentView, GameOptions {

    companion object {
        private const val TAG = "BaseView"
    }

    @Composable
    abstract fun ToolBarMenu(modifier: Modifier)
    @Composable
    abstract fun GameViewGrid()
    @Composable
    abstract fun CreateNewGameDialog()

    abstract fun getCurrentPresenter(): BasePresenter?
    abstract fun getCurrentViewModel(): BaseViewModel?
    abstract fun setHasNextForView(hasNext: Boolean)
    abstract fun ifInterstitialWhenSaveScore()
    abstract fun ifInterstitialWhenNewGame()
    abstract fun ifCreatingNewGame(newGameLevel: Int, originalLevel: Int)
    open fun isFiveBalls() = false
    open fun actionOnClick() {}
    open fun stopActionOnClick() {}
    open fun setTheGameLevel(gameLevel: Int) = baseViewModel.setGameLevel(gameLevel)

    var menuBarWeight = 1.0f
    var gameGridWeight = 7.0f
    var gameWidthRation = 1.0f
    val colorPrimary = Color(0xFF3F51B5)
    val colorYellow3 = Yellow3
    val mOrientation = mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT)
    var boxImage: Bitmap? = null
    var mImageSizeDp = 0f
    val colorBallMap: HashMap<Int, Bitmap> = HashMap()
    val colorOvalBallMap: HashMap<Int, Bitmap> = HashMap()
    val colorNextBallMap: HashMap<Int, Bitmap> = HashMap()
    var interstitialAd: ShowInterstitial? = null

    private var textFontSize = 0f
    private var toastTextSize = 0f
    private var mBaseApp: BaseApp? = null
    private lateinit var basePresenter: BasePresenter
    lateinit var baseViewModel: BaseViewModel
    private lateinit var mGameOptions: GameOptions

    // the following are for Top 10 Players
    // the following are for Settings
    private lateinit var settingLauncher: ActivityResultLauncher<Intent>
    private lateinit var top10Launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate")
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@BaseView)
        toastTextSize = textFontSize * 0.7f
        CbComposable.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp
        CbComposable.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp

        mOrientation.intValue = resources.configuration.orientation
        mBaseApp = application as? BaseApp

        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG) {
            val deviceType = ScreenUtil.getDeviceType(this@BaseView)
            requestedOrientation = if (deviceType == ScreenUtil.DEVICE_TYPE_PHONE) {
                // phone then change orientation to Portrait
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                // Table then change orientation to Landscape
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        LogUtil.d(TAG, "$TAG.onCreate.interstitialAd")
        interstitialAd = ShowInterstitial(this, null,
            mBaseApp?.getInterstitial())

        getCurrentPresenter()?.let {
            basePresenter = it
        } ?: run {
            LogUtil.d(TAG, "$TAG.onCreate.basePresenter is null so exit activity.")
            return
        }
        getCurrentViewModel()?.let {
            baseViewModel = it
        } ?: run {
            LogUtil.d(TAG, "$TAG.onCreate.baseViewModel is null so exit activity.")
            return
        }
        // The following statements must be after having basePresenter and baseViewModel
        mGameOptions = this as GameOptions
        mGameOptions.setWhichGame()

        top10Launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "top10Launcher.result = $result")
        }

        settingLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "$TAG.settingLauncher.result received")
            if (result.resultCode == RESULT_OK) {
                val originalLevel = baseViewModel.getGameLevel()
                var newGameLevel: Int
                val data = result.data ?: return@registerForActivityResult
                data.extras?.let { extras ->
                    baseViewModel.setHasSound(extras.getBoolean(Constants.HAS_SOUND,
                        true))
                    newGameLevel = extras.getInt(Constants.GAME_LEVEL,
                        originalLevel)
                    setTheGameLevel(newGameLevel)
                    // baseViewModel.setGameLevel(newGameLevel)
                    val hasNext = extras.getBoolean(Constants.HAS_NEXT,true)
                    setHasNextForView(hasNext)
                    ifCreatingNewGame(newGameLevel, originalLevel)
                }
            }
        }
        val adWeight = 10.0f - menuBarWeight - gameGridWeight
        LogUtil.i(TAG, "$TAG.onCreate.menuBarWeight = $menuBarWeight")
        LogUtil.i(TAG, "$TAG.onCreate.gameGridWeight = $gameGridWeight")
        LogUtil.i(TAG, "$TAG.onCreate.adWeight = $adWeight")
        // enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LogUtil.i(TAG, "$TAG.onCreate.setContent")
            ColorBallsTheme {
                Scaffold {innerPadding ->
                    Box(Modifier.padding(innerPadding)
                        .background(color = colorYellow3)) {
                        if (mOrientation.intValue ==
                            Configuration.ORIENTATION_PORTRAIT) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                GameView(Modifier.weight(gameGridWeight))
                                SHowPortraitAds(Modifier.fillMaxWidth()
                                        .weight(adWeight))
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
                LogUtil.i(TAG, "$TAG.onCreate.setContent.LaunchedEffect")
                baseViewModel.initGame(savedInstanceState)
            }
        }

        onBackPressedDispatcher.addCallback(object
            : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtil.d(TAG, "$TAG.onBackPressedDispatcher.handleOnBackPressed")
                onBackWasPressed()
            }
        })
    }

    // should be private after UI migration
    fun onBackWasPressed() {
        LogUtil.d(TAG, "onBackWasPressed")
        // capture the event of back button when it is pressed
        // change back button behavior
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            baseViewModel.quitGame() //   from   END PROGRAM
        } else {
            exitAppTimer.start()
            val toastFontSize = textFontSize * 0.7f
            LogUtil.d(TAG, "toastFontSize = $toastFontSize")
            ScreenUtil.showToast(
                this@BaseView,
                getString(R.string.backKeyToExitApp),
                toastFontSize,
                ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LogUtil.i(TAG, "onConfigurationChanged.newConfig.orientation = " +
                "${newConfig.orientation}")
        mOrientation.intValue = newConfig.orientation
    }

    override fun onSaveInstanceState(outState: Bundle) {
        LogUtil.i(TAG, "onSaveInstanceState")
        baseViewModel.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        LogUtil.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        LogUtil.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        LogUtil.i(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        baseViewModel.release()
        interstitialAd?.releaseInterstitial()
    }

    protected fun showInterstitialAd() {
        LogUtil.i(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    protected fun bitmapDrawableResources(sizePx: Float) {
        LogUtil.i(TAG, "bitmapDrawableResources.imageSizePx = $sizePx")
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

        BitmapFactory.decodeResource(resources, R.drawable.firework)?.let { bm ->
            colorBallMap[Constants.COLOR_FIREWORK] =
                bm.scale(ballWidth, ballHeight)
            colorNextBallMap[Constants.COLOR_FIREWORK] =
                bm.scale(nextBallWidth, nextBallHeight)
            colorOvalBallMap[Constants.COLOR_FIREWORK] =
                bm.scale(ovalBallWidth, ovalBallHeight)
        }
    }

    private fun finishThisActivity() {
        LogUtil.i(TAG, "finishThisActivity = $interstitialAd")
        finish()
    }

    private fun exitApplication() {
        LogUtil.i(TAG, "exitApplication")
        /*
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 1000
        // exit application
        handlerClose.postDelayed({ finishThisActivity() },
            timeDelay.toLong())
        */
        finishThisActivity()
    }

    fun quitOrNewGame() {
        LogUtil.i(TAG, "quitOrNewGame")
        if (baseViewModel.mGameAction == Constants.IS_QUITING_GAME) {
            //  END PROGRAM
            LogUtil.i(TAG, "quitOrNewGame.exitApplication")
            exitApplication()
        } else if (baseViewModel.mGameAction == Constants.IS_CREATING_GAME) {
            //  NEW GAME
            LogUtil.i(TAG, "quitOrNewGame.ifInterstitialWhenNewGame")
            ifInterstitialWhenNewGame()
        }
        baseViewModel.setSaveScoreAlertDialogState(false)
        // baseViewModel.isProcessingJob = false
    }

    @Composable
    fun getContentHeight(): Point {
        val density = LocalDensity.current
        // Get the height of the top status bar
        val statusBarHeight = WindowInsets.safeDrawing.getTop(density)
        LogUtil.d(TAG, "getContentHeight.statusBarHeight = $statusBarHeight")
        // Get the height of the bottom navigation bar
        val navigationBarHeight = WindowInsets.safeDrawing.getBottom(density)
        LogUtil.d(TAG, "getContentHeight.navigationBarHeight = $navigationBarHeight")
        // Get the total screen height
        val screenWidth = with(density) {
            LocalConfiguration.current.screenWidthDp
        }
        var screenHeight = with(density) {
            LocalConfiguration.current.screenHeightDp
        }
        // Calculate the available content height
        // WindowCompat.setDecorFitsSystemWindows(window, false)
        // and     Scaffold {innerPadding ->
        // removes the navigationBarHeight
        screenHeight -= statusBarHeight // - navigationBarHeight
        return Point(screenWidth, screenHeight)
    }

    // this fun will be override in com.smile.fivecolorballs
    @SuppressLint("ConfigurationScreenWidthHeight")
    @Composable
    open fun GameView(modifier: Modifier) {
        LogUtil.i(TAG, "GameView.mOrientation.intValue = ${mOrientation.intValue}")
        val screen = getContentHeight()
        LogUtil.d(TAG, "GameView.screen.x = ${screen.x}")
        LogUtil.d(TAG, "GameView.screen.y = ${screen.y}")

        var maxWidth = screen.x
        if (mOrientation.intValue == Configuration.ORIENTATION_LANDSCAPE) {
            LogUtil.d(TAG, "GameView.ORIENTATION_LANDSCAPE")
            maxWidth = screen.x/2
        }
        maxWidth = (maxWidth.toFloat() * gameWidthRation).toInt()
        val gridHeight = screen.y * gameGridWeight / 10.0f
        LogUtil.d(TAG, "GameView.gridHeight = $gridHeight")
        val heightPerBall = gridHeight / baseViewModel.rowCounts
        LogUtil.d(TAG, "GameView.heightPerBall = $heightPerBall")
        val widthPerBall = maxWidth / baseViewModel.colCounts
        LogUtil.d(TAG, "GameView.widthPerBall = $widthPerBall")

        // set size of color balls
        val imageSizeDp = (if (heightPerBall>widthPerBall) widthPerBall
        else heightPerBall).toInt()
        LogUtil.d(TAG, "GameView.imageSizeDp = $imageSizeDp")
        val imageSizePx: Float = with(LocalDensity.current) {
            imageSizeDp.dp.toPx()
        }
        bitmapDrawableResources(imageSizePx)
        mImageSizeDp = imageSizeDp.toFloat()
        LogUtil.d(TAG, "GameView.mImageSizeDp = $mImageSizeDp")

        val topPadding = 0f
        val gHeightWeight = if (menuBarWeight>0) gameGridWeight else 1.0f
        Column(modifier = modifier.fillMaxHeight()) {
            if (menuBarWeight > 0.0f) {
                ToolBarMenu(
                    modifier = Modifier
                        .weight(menuBarWeight)
                        .padding(top = topPadding.dp, start = 0.dp)
                )
            }
            Column(modifier = Modifier.weight(gHeightWeight)) {
                GameViewGrid()
            }
        }
    }

    @Composable
    fun ShowCurrentScore(modifier: Modifier, pFontSize: TextUnit) {
        LogUtil.i(TAG, "ShowCurrentScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = baseViewModel.getCurrentScore().toString(),
            modifier = modifier,
            color = Color.Red, fontSize = pFontSize
        )
    }

    @Composable
    fun SHowHighestScore(modifier: Modifier, pFontSize: TextUnit) {
        LogUtil.i(TAG, "SHowHighestScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = baseViewModel.getHighestScore().toString(),
            modifier = modifier,
            color = Color.White, fontSize = pFontSize
        )
    }

    @Composable
    fun SaveGameDialog() {
        LogUtil.i(TAG, "SaveGameDialog")
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
                    ScreenUtil.showToast(this@BaseView, msg, textFontSize,
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
                this@BaseView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun LoadGameDialog() {
        LogUtil.i(TAG, "LoadGameDialog")
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
                    ScreenUtil.showToast(this@BaseView, msg, textFontSize,
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
                this@BaseView,
                buttonListener, "", dialogText
            )
        }
    }

    @Composable
    fun SaveScoreDialog() {
        LogUtil.i(TAG, "SaveScoreDialog")
        val dialogTitle = baseViewModel.getSaveScoreTitle()
        if (dialogTitle.isNotEmpty()) {
            baseViewModel.setSaveScoreAlertDialogState(true)
            val buttonListener = object: CbComposable.ButtonClickListenerString {
                override fun buttonOkClick(value: String?) {
                    LogUtil.d(TAG, "SaveScoreDialog.buttonOkClick.value = $value")
                    baseViewModel.saveScore(value ?: "No Name")
                    baseViewModel.setSaveScoreTitle("")
                    quitOrNewGame()
                }
                override fun buttonCancelClick(value: String?) {
                    LogUtil.d(TAG, "SaveScoreDialog.buttonCancelClick.value = $value")
                    // set SaveScoreDialog() invisible
                    baseViewModel.setSaveScoreTitle("")
                    quitOrNewGame()
                }
            }
            val hitStr = getString(R.string.nameStr)
            CbComposable.DialogWithTextField(
                this@BaseView,
                buttonListener, dialogTitle, hitStr
            )
        } else {
            ifInterstitialWhenSaveScore()
        }
    }

    @Composable
    fun ShowMessageOnScreen() {
        LogUtil.i(TAG, "ShowMessageOnScreen.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val message = baseViewModel.getScreenMessage()
        LogUtil.i(TAG, "ShowMessageOnScreen.message = $message")
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
        LogUtil.i(TAG, "SHowPortraitAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")

        val adWidth = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width
                .toDp().value.toInt()
        }

        Column(modifier = modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            mBaseApp?.let {
                CbComposable.ShowAdmobNormalBanner(modifier = Modifier.weight(1.0f),
                    it.getBannerID())
                CbComposable.ShowAdmobAdaptiveBanner(modifier = Modifier.weight(1.0f),
                    it.getBannerID2(), adWidth)
            }
        }
    }

    @Composable
    fun ShowNativeAd(modifier: Modifier = Modifier) {
        LogUtil.i(TAG, "ShowNativeAd.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
        LaunchedEffect(Unit) {
            mBaseApp?.let {
                object : GoogleNativeAd(
                    this@BaseView,it.getNativeID()) {
                    override fun setNativeAd(ad: NativeAd?) {
                        LogUtil.d(TAG, "ShowNativeAd.GoogleNativeAd.setNativeAd")
                        nativeAd = ad
                    }
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
                        LogUtil.d(TAG, "ShowNativeAd.callToAction.cta = $cta")
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
        LogUtil.i(TAG, "ShowLandscapeAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        /*
        val colHeight = with(LocalDensity.current) {
            screenY.toDp()
        }
        */
        Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            ShowNativeAd(modifier = Modifier.weight(8.0f))
            mBaseApp?.let {
                CbComposable.ShowAdmobAdaptiveBanner(
                    modifier = Modifier.weight(2.0f),
                    it.getBannerID2(), 0)
            }
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
        LogUtil.i(TAG, "showTop10Players.isLocal = $isLocal")
        Intent(
            this@BaseView,
            Top10Activity::class.java
        ).let {
            Bundle().apply {
                putString(Constants.GAME_ID, GameUtil.getGameId(baseViewModel.getWhichGame()))
                putString(Constants.DATABASE_NAME, GameUtil.getDatabaseName(baseViewModel.getWhichGame()))
                putBoolean(Constants.IS_LOCAL_TOP10, isLocal)
                it.putExtras(this)
                top10Launcher.launch(it)
            }
        }
    }

    private fun onClickSettingButton() {
        if (baseViewModel.isProcessingJob()) return
        actionOnClick()
        Intent(
            this@BaseView,
            CbSettingActivity::class.java
        ).let {
            Bundle().apply {
                putString(Constants.GAME_ID,
                    GameUtil.getGameId(baseViewModel.getWhichGame()))
                putBoolean(Constants.HAS_SOUND, baseViewModel.hasSound())
                putInt(Constants.GAME_LEVEL, baseViewModel.getGameLevel())
                putBoolean(Constants.HAS_NEXT, baseViewModel.hasNext())
                it.putExtras(this)
                settingLauncher.launch(it)
            }
        }
    }

    private fun showSmileAppsActivity() {
        Intent(
            this@BaseView,
            SmileAppsActivity::class.java
        ).also {
            startActivity(it)
        }
    }

    @Composable
    fun UndoButton(modifier: Modifier) {
        LogUtil.i(TAG, "UndoButton.mOrientation.intValue" +
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
        LogUtil.i(TAG, "SettingButton.mOrientation.intValue" +
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
        LogUtil.i(TAG, "ShowMenu.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val dropdownWidth =
            if (mOrientation.intValue == Configuration.ORIENTATION_PORTRAIT) {
                mImageSizeDp * 6.0f
            } else {
                mImageSizeDp * 8.0f
            }
        var expanded by remember { mutableStateOf(false) }
        LogUtil.d(TAG, "ShowMenu.expanded = $expanded")
        Column(modifier = modifier) {
            IconButton (onClick = {
                if (!baseViewModel.isProcessingJob()) {
                    expanded = !expanded
                    if (expanded) actionOnClick() else stopActionOnClick()
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
                onDismissRequest = {
                    expanded = false
                    stopActionOnClick()
                                   },
                modifier = Modifier
                    .requiredHeightIn(max = (mImageSizeDp * 12f).dp)
                    .requiredWidth(dropdownWidth.dp)
                    .background(
                        // Color(getColor(android.R.color.holo_green_light))
                        color = Color(0xff99cc00)
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

                if (!isFiveBalls()) {
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
                }

                val isAppListClicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.smileApps),
                    color = if (isAppListClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isAppListClicked)
                        showSmileAppsActivity()
                    })

                val isPrivacyClicked = remember { mutableStateOf(false) }
                CbComposable.DropdownMenuItem(
                    text = getString(R.string.privacyPolicyString),
                    color = if (isPrivacyClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isPrivacyClicked)
                        PrivacyPolicyUtil.startPrivacyPolicyActivity(
                            this@BaseView, 10)
                    },
                    isDivider = false)
            }
        }
    }

    @Composable
    fun ShowGameGrid(isClickable: Boolean = true) {
        LogUtil.i(TAG, "ShowGameGrid.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Column {
            for (i in 0 until baseViewModel.rowCounts) {
                Row {
                    for (j in 0 until baseViewModel.colCounts) {
                        Box(Modifier
                            .clickable(enabled = isClickable) {
                                baseViewModel.cellClickListener(i, j)
                            }
                            /*
                            // no need
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState(onDelta = {}),
                                enabled = false)
                            */
                        ) {
                            Image(
                                modifier = Modifier
                                    // .size(mImageSizeDp.dp)   // image already resized
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
        LogUtil.d(TAG, "ShowColorBall.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        ShowBall(baseViewModel.gridDataArray[i][j].value)
    }

    @Composable
    fun ShowBall(ballInfo: ColorBallInfo) {
        LogUtil.d(TAG, "ShowBall.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val ballColor = ballInfo.ballColor
        val isAnimation = ballInfo.isAnimation
        val isReSize = ballInfo.isResize
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
        LogUtil.d(TAG, "soundPool")
        val sPool = SoundPoolUtil(this, R.raw.uhoh)
        LogUtil.d(TAG, "soundPool.sPool = $sPool")
        return sPool
    }

    override fun getRoomDatabase(): ScoreDatabase {
        return ScoreDatabase.getDatabase(this,
            GameUtil.getDatabaseName(baseViewModel.getWhichGame()))
    }

    override fun fileInputStream(fileName : String): FileInputStream {
        return FileInputStream(File(filesDir, fileName))
    }

    override fun fileOutputStream(fileName : String): FileOutputStream {
        return FileOutputStream(File(filesDir, fileName))
    }
    // end of implementing
}