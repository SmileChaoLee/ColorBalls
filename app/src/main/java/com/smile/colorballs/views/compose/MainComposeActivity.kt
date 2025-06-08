package com.smile.colorballs.views.compose

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.gms.ads.nativead.NativeAd
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.shared_composables.ui.theme.ColorBallsTheme
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.GoogleNativeAd
import com.smile.colorballs.models.Settings
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import com.smile.smilelibraries.utilities.ScreenUtil

class MainComposeActivity : MyComposeView() {
    companion object {
        private const val TAG = "MainComposeActivity"
    }

    private val mOrientation = mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT)
    private var screenX = 0f
    private var screenY = 0f
    private val gameGridWeight = 0.7f
    // the following are for Top 10 Players
    private lateinit var top10Launcher: ActivityResultLauncher<Intent>
    // the following are for Settings
    private lateinit var settingLauncher: ActivityResultLauncher<Intent>
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$TAG.onCreate")
        Log.d(TAG, "onCreate.getScreenSize()")
        getScreenSize()

        top10Launcher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            Log.d(TAG, "top10Launcher.result received")
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "top10Launcher.Showing interstitial ads")
                showInterstitialAd()
            }
        }

        settingLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "settingLauncher.result received")
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                data.extras?.let { extras ->
                    mPresenter.setHasSound(extras.getBoolean(Constants.HAS_SOUND,
                        true))
                    mPresenter.setEasyLevel(extras.getBoolean(Constants.IS_EASY_LEVEL,
                        true))
                    mPresenter.setHasNextBall(extras.getBoolean(Constants.HAS_NEXT_BALL,
                        true),true)
                }
                Log.d(TAG, "settingLauncher.Showing interstitial ads")
                showInterstitialAd()
            }
        }

        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                mOrientation.intValue = resources.configuration.orientation
                val backgroundColor = Color(getColor(R.color.yellow3))
                Box(Modifier.background(color = backgroundColor)) {
                    if (mOrientation.intValue ==
                        Configuration.ORIENTATION_PORTRAIT) {
                        Column {
                            CreateMainUI(Modifier.weight(gameGridWeight))
                            SHowPortraitAds(Modifier.fillMaxWidth()
                                .weight(1.0f - gameGridWeight))
                        }
                    } else {
                        Row {
                            CreateMainUI(Modifier.weight(1f))
                            ShowLandscapeAds(modifier = Modifier.weight(1f))
                        }
                    }
                    Top10PlayerUI()
                    SettingUI()
                    Box {
                        SaveGameDialog()
                        LoadGameDialog()
                        GameOverDialog()
                        SaveScoreDialog()
                    }
                }
            }
            LaunchedEffect(Unit) {
                Log.d(TAG, "onCreate.setContent.LaunchedEffect")
                mPresenter.initGame(savedInstanceState)
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                onBackWasPressed()
            }
        })
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged.newConfig.orientation = " +
                "${newConfig.orientation}")
        mOrientation.intValue = newConfig.orientation
        getScreenSize()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        if (isChangingConfigurations) {
            // configuration is changing then remove top10Fragment
            ColorBallsApp.isShowingLoadingMessage = false
            ColorBallsApp.isProcessingJob = false
        }
        mPresenter.onSaveInstanceState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private fun onBackWasPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            mPresenter.quitGame() //   from   END PROGRAM
        } else {
            exitAppTimer.start()
            val toastFontSize = textFontSize * 0.7f
            Log.d(TAG, "toastFontSize = $toastFontSize")
            ScreenUtil.showToast(
                this@MainComposeActivity,
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

    @Composable
    fun CreateMainUI(modifier: Modifier) {
        Log.d(TAG, "CreateMainUI.mOrientation = $mOrientation")
        GameView(modifier)
        Log.d(TAG, "CreateMainUI.mImageSize = $mImageSizeDp")
    }

    @Composable
    fun Top10PlayerUI() {
        Log.d(TAG, "Top10PlayerUI.mOrientation.intValue " +
                "= ${mOrientation.intValue}")
        val title = viewModel.top10TitleName.value
        if (title.isNotEmpty()) {
            var isDialogOpen by remember { mutableStateOf(true) }
            Dialog(onDismissRequest = { isDialogOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                content = {
                    Composables.Top10Composable(
                        title = title,
                        topPlayers = viewModel.top10Players.value, buttonListener =
                        object : Composables.ButtonClickListener {
                            override fun buttonOkClick() {
                                isDialogOpen = false
                                showInterstitialAd()
                                viewModel.setTop10TitleName("")
                            }
                        },
                        getString(R.string.okStr)
                    )
                })
        }
    }

    @Composable
    fun SettingUI() {
        Log.d(TAG, "SettingUI.mOrientation.intValue " +
                "= ${mOrientation.intValue}")
        if (viewModel.settingTitle.value.isNotEmpty()) {
            var isDialogOpen by remember { mutableStateOf(true) }
            val setting = Settings(mPresenter.hasSound(),
                mPresenter.isEasyLevel(), mPresenter.hasNextBall())
            val textClick = object : Composables.SettingClickListener {
                override fun hasSoundClick(hasSound: Boolean) {
                    Log.d(TAG, "textClick.hasSoundClick.hasSound = $hasSound")
                    setting.hasSound = hasSound
                }
                override fun easyLevelClick(easyLevel: Boolean) {
                    Log.d(TAG, "textClick.easyLevelClick.easyLevel = $easyLevel")
                    setting.easyLevel = easyLevel
                }
                override fun hasNextClick(hasNext: Boolean) {
                    Log.d(TAG, "textClick.hasNextClick.hasNext = $hasNext")
                    setting.hasNextBall = hasNext
                }
            }

            val buttonClick = object : Composables.ButtonClickListener  {
                override fun buttonOkClick() {
                    viewModel.setSettingTitle("")
                    isDialogOpen = false
                    mPresenter.setHasSound(setting.hasSound)
                    mPresenter.setEasyLevel(setting.easyLevel)
                    mPresenter.setHasNextBall(setting.hasNextBall, true)
                    showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    isDialogOpen = false
                    viewModel.setSettingTitle("")
                    showInterstitialAd()
                }
            }

            Dialog(onDismissRequest = { isDialogOpen = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                content = {
                    Composables.SettingCompose(
                        this@MainComposeActivity,
                        buttonClick, textClick, getString(R.string.settingStr),
                        backgroundColor = Color(0xbb0000ff), setting
                    )
                })
        }
    }

    @Composable
    fun GameView(modifier: Modifier) {
        Log.d(TAG, "GameView.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Log.d(TAG, "GameView.screenX = $screenX, screenY = $screenY")
        val maxHeight = screenY
        Log.d(TAG, "GameView.maxHeight = $maxHeight")
        var maxWidth: Float
        var barHeight: Float
        val adHeight: Float
        val gHeight: Float
        val orientation = mOrientation.intValue
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "GameView.ORIENTATION_PORTRAIT")
            maxWidth = screenX
            barHeight = (maxHeight * 1.2f) / 10f
            adHeight = maxHeight * (1.0f - gameGridWeight)
            gHeight = maxHeight - barHeight - adHeight
        } else {
            Log.d(TAG, "GameView.ORIENTATION_LANDSCAPE")
            maxWidth = screenX / 2f
            barHeight = (maxHeight * 1.2f) / 10f
            adHeight = maxWidth
            gHeight = maxHeight - barHeight
        }
        Log.d(TAG, "GameView.maxWidth = $maxWidth")
        Log.d(TAG, "GameView.barHeight = $barHeight")
        Log.d(TAG, "GameView.adHeight = $adHeight")
        Log.d(TAG, "GameView.gHeight = $gHeight")

        val gameSize  = if (gHeight > maxWidth) maxWidth else gHeight
        Log.d(TAG, "GameView.gameSize = $gameSize")
        val imageSizePx = (gameSize / (Constants.ROW_COUNTS.toFloat()))
        Log.d(TAG, "GameView.imageSizePx = $imageSizePx")
        var realGameSize = (imageSizePx * Constants.ROW_COUNTS.toFloat())
        Log.d(TAG, "GameView.realGameSize = $realGameSize")
        var startPadding = ((maxWidth - realGameSize) / 2f).coerceAtLeast(0f)
        Log.d(TAG, "GameView.startPadding = $startPadding")

        mImageSizeDp = ScreenUtil.pixelToDp(imageSizePx)
        Log.d(TAG, "GameView.mImageSizeDp = $mImageSizeDp")
        // set size of color balls
        bitmapDrawableResources()

        maxWidth = ScreenUtil.pixelToDp(maxWidth)
        barHeight = ScreenUtil.pixelToDp(barHeight)
        realGameSize = ScreenUtil.pixelToDp(realGameSize)
        startPadding = ScreenUtil.pixelToDp(startPadding).toInt().toFloat()
        Log.d(TAG, "GameView.startPadding.pixelToDp = $startPadding")
        // adHeight = ScreenUtil.pixelToDp(adHeight)

        val topPadding = 0f
        // val backgroundColor = Color(getColor(R.color.yellow3))
        Column(modifier = modifier.fillMaxHeight()
            // .background(color = backgroundColor)
            .width(width = maxWidth.dp)) {
            ToolBarMenu(modifier = Modifier.height(height = barHeight.dp)
                .padding(top = topPadding.dp, start = 0.dp))
            GameViewGrid(modifier = Modifier.height(height = realGameSize.dp)
                    .padding(top = 0.dp, start = startPadding.dp))
        }
    }

    @Composable
    fun ToolBarMenu(modifier: Modifier) {
        Log.d(TAG, "ToolBarMenu.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Row(modifier = modifier
            .background(color = Color(getColor(R.color.colorPrimary)))) {
            ShowCurrentScore(
                Modifier.weight(2f).padding(start = 10.dp)
                    .align(Alignment.CenterVertically))
            SHowHighestScore(
                Modifier.weight(2f)
                    .align(Alignment.CenterVertically))
            UndoButton(modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically))
            SettingButton(modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically))
            ShowMenu(modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically))
        }
    }

    @Composable
    fun ShowCurrentScore(modifier: Modifier) {
        Log.d(TAG, "ShowCurrentScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = mPresenter.currentScore.intValue.toString(),
            modifier = modifier,
            color = Color.Red, fontSize = Composables.mFontSize
        )
    }

    @Composable
    fun SHowHighestScore(modifier: Modifier) {
        Log.d(TAG, "SHowHighestScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = mPresenter.highestScore.intValue.toString(),
            modifier = modifier,
            color = Color.White, fontSize = Composables.mFontSize
        )
    }

    @Composable
    fun UndoButton(modifier: Modifier) {
        Log.d(TAG, "UndoButton.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        IconButton (onClick = { mPresenter.undoTheLast() },
            modifier = modifier
            /*, colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent) */ ) {
            Icon(
                painter = painterResource(R.drawable.undo),
                contentDescription = "",
                tint = Color.White
            )
        }
    }

    private fun onClickSettingButton(useActivity: Boolean) {
        if (useActivity) {
            Intent(
                this@MainComposeActivity,
                SettingComposeActivity::class.java
            ).let {
                Bundle().apply {
                    putBoolean(Constants.HAS_SOUND, mPresenter.hasSound())
                    putBoolean(Constants.IS_EASY_LEVEL, mPresenter.isEasyLevel())
                    putBoolean(Constants.HAS_NEXT_BALL, mPresenter.hasNextBall())
                    it.putExtras(this)
                    settingLauncher.launch(it)
                }
            }
        } else {
            viewModel.setSettingTitle(getString(R.string.settingStr))
        }
    }

    @Composable
    fun SettingButton(modifier: Modifier) {
        Log.d(TAG, "SettingButton.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        IconButton (onClick = { onClickSettingButton(useActivity = false) },
            modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.setting),
                contentDescription = "",
                tint = Color.White
            )
        }
    }

    private fun showTop10Players(isLocal: Boolean, useActivity: Boolean) {
        Log.d(TAG, "showTop10Players.isLocal " +
                "= $isLocal , useActivity = $useActivity")
        if (useActivity) {
            Intent(
                this@MainComposeActivity,
                Top10ComposeActivity::class.java
            ).let {
                Bundle().apply {
                    putBoolean(Constants.IS_LOCAL_TOP10, isLocal)
                    it.putExtras(this)
                    top10Launcher.launch(it)
                }
            }
        } else {
            viewModel.setTop10TitleName(
                if (isLocal) getString(R.string.localTop10Score) else
                    getString(R.string.globalTop10Str)
            )
            viewModel.getTop10Players(context = this@MainComposeActivity, isLocal)
        }
    }

    @Composable
    fun ShowMenu(modifier: Modifier) {
        Log.d(TAG, "ShowMenu.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = modifier) {
            IconButton (onClick = { expanded = !expanded }, modifier = modifier) {
                Icon(
                    painter = painterResource(R.drawable.three_dots),
                    contentDescription = "",
                    tint = Color.White
                )
            }
            DropdownMenu(expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.requiredHeightIn(max = (mImageSizeDp*9f).dp)
                    .background(color =
                Color(getColor(android.R.color.holo_green_light)))
                    .padding(all = 0.dp)
            ) {
                Composables.DropdownMenuItem(
                    text = getString(R.string.globalTop10Str),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        showTop10Players(isLocal = false, useActivity = false)
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.localTop10Score),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        showTop10Players(isLocal = true, useActivity = false)
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.saveGameStr),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        mPresenter.saveGame()
                        // mPresenter.setSaveGameTitle(
                        //     getString(R.string.sureToSaveGameStr))
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.loadGameStr),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        mPresenter.loadGame()
                        // mPresenter.setLoadGameTitle(
                        //     getString(R.string.sureToLoadGameStr))
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.newGame),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        mPresenter.newGame()
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.quitGame),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        mPresenter.quitGame()
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.privacyPolicyString),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        PrivacyPolicyUtil.startPrivacyPolicyActivity(
                            this@MainComposeActivity, 10)
                              },
                    isDivider = false)
            }
        }
    }

    @Composable
    fun GameViewGrid(modifier: Modifier) {
        Log.d(TAG, "GameViewGrid.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Column(modifier = modifier) {
            Box {
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
        boxImage?.let {
            Log.d(TAG, "ShowGameGrid.boxImage.width = ${it.width}")
            Log.d(TAG, "ShowGameGrid.boxImage.height = ${it.height}")
        }
        // val width0 = (mImageSizeDp * (Constants.ROW_COUNTS.toFloat()))
        Column {
            for (i in 0 until Constants.ROW_COUNTS) {
                // Row(modifier = Modifier.width(width0.dp)) {
                Row {
                    for (j in 0 until Constants.ROW_COUNTS) {
                        Box(Modifier
                            .clickable {
                                mPresenter.drawBallsAndCheckListener(i, j)
                        }) {
                            Image(
                                modifier = Modifier.size(mImageSizeDp.dp)
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
    fun ShowMessageOnScreen() {
        Log.d(TAG, "ShowMessageOnScreen.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val message = mPresenter.screenMessage.value
        if (message.isEmpty()) return
        val gameViewLength = mImageSizeDp * Constants.ROW_COUNTS.toFloat()
        val width = (gameViewLength/2f).dp
        val height = (gameViewLength/4f).dp
        /*
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            width = (screenY.floatValue/6f).dp
            height = (screenX.floatValue/3f).dp
        }
         */
        val modifier = Modifier.fillMaxSize()
            .background(color = Color.Transparent)
        Column( modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(width = width, height = height)){
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
    }

    @Composable
    fun ShowColorBall(i: Int, j: Int) {
        Log.d(TAG, "ShowColorBall.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val ballInfo = mPresenter.gridDataArray[i][j].value
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
            WhichBall.NEXT_BALL-> { colorNextBallMap.getValue(ballColor) }
            WhichBall.NO_BALL -> { null }
        }
        Column(modifier = Modifier.size(mImageSizeDp.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            var modifier = Modifier.background(color = Color.Transparent)
            var scale: ContentScale = ContentScale.Inside
            if (isReSize) {
                modifier = modifier.size(mImageSizeDp.dp).padding(all = 0.dp)
                scale = ContentScale.Fit
            }
            Image(
                painter = BitmapPainter(bitmap!!.asImageBitmap()),
                contentDescription = "",
                modifier = modifier,
                contentScale = scale
            )
        }
    }

    @Composable
    fun SHowPortraitAds(modifier: Modifier) {
        Log.d(TAG, "SHowPortraitAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val adWidth = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width
                .toDp().value.toInt()
        }
        Column(modifier = modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
                ShowAdmobNormalBanner(modifier = modifier)
                ShowAdmobAdaptiveBanner(modifier = modifier, width = adWidth)
        }
    }

    @Composable
    fun ShowNativeAd(modifier: Modifier = Modifier) {
        Log.d(TAG, "ShowNativeAd.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
            LaunchedEffect(Unit) {
                object : GoogleNativeAd(this@MainComposeActivity,
                    ColorBallsApp.googleAdMobNativeID) {
                    override fun setNativeAd(ad: NativeAd?) {
                        Log.d(TAG, "ShowNativeAd.GoogleNativeAd.setNativeAd")
                        nativeAd = ad
                    }
                }
            }
            nativeAd?.let {
                MyNativeAdView(ad = it) { ad, _ ->
                    Row(modifier = Modifier.weight(1f)) {
                        val icon: Drawable? = ad.icon?.drawable
                        icon?.let { drawable ->
                            Image(
                                painter = rememberDrawablePainter(drawable = drawable),
                                contentDescription = ""/*ad.icon?.contentDescription*/,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ShowLandscapeAds(modifier: Modifier) {
        Log.d(TAG, "ShowLandscapeAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Column(modifier = modifier.fillMaxHeight().
            fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            ShowNativeAd(modifier = Modifier.weight(8.0f))
            ShowAdmobNormalBanner(modifier = Modifier.weight(2.0f))
        }
    }
}
