package com.smile.colorballs.views

import android.content.Intent
import android.content.pm.ActivityInfo
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.gms.ads.nativead.NativeAd
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.views.ui.theme.ColorBallsTheme
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.smilelibraries.GoogleNativeAd
import com.smile.colorballs.views.ui.theme.ColorPrimary
import com.smile.colorballs.views.ui.theme.Yellow3
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class ColorBallActivity : MyView() {
    private var mTAG : String = "ColorBallActivity"
    fun setTag(tag: String) {
        Log.d(mTAG, "setTag.tag = $tag")
        mTAG = tag
    }

    private val mOrientation = mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT)
    private var screenX = 0f
    private var screenY = 0f
    private val gameGridWeight = 7.0f

    /**
     * whichGame = 0 : Empty distribution
     * whichGame = 1 : Random distribution
     */
    var whichGame = 0
    // the following are for Top 10 Players
    private lateinit var top10Launcher: ActivityResultLauncher<Intent>
    // the following are for Settings
    private lateinit var settingLauncher: ActivityResultLauncher<Intent>
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(mTAG, "$mTAG.onCreate")

        requestedOrientation = if (ScreenUtil.isTablet(this@ColorBallActivity)) {
            // Table then change orientation to Landscape
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            // phone then change orientation to Portrait
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        Log.d(mTAG, "onCreate.getScreenSize()")
        getScreenSize()

        top10Launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            Log.d(mTAG, "top10Launcher.result received")
            /*
            if (result.resultCode == RESULT_OK) {
                Log.d(mTAG, "top10Launcher.Showing interstitial ads")
                showInterstitialAd()
            }
            */
            showInterstitialAd()
        }

        settingLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(mTAG, "settingLauncher.result received")
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                data.extras?.let { extras ->
                    viewModel.setHasSound(extras.getBoolean(Constants.HAS_SOUND,
                        true))
                    viewModel.setEasyLevel(extras.getBoolean(Constants.IS_EASY_LEVEL,
                        true))
                    viewModel.setHasNextBall(extras.getBoolean(Constants.HAS_NEXT_BALL,
                        true),true)
                }
                Log.d(mTAG, "settingLauncher.Showing interstitial ads")
            }
            showInterstitialAd()
        }

        setContent {
            Log.d(mTAG, "onCreate.setContent")
            ColorBallsTheme {
                mOrientation.intValue = resources.configuration.orientation
                val backgroundColor = Yellow3
                Box(Modifier.background(color = backgroundColor)) {
                    if (mOrientation.intValue ==
                        Configuration.ORIENTATION_PORTRAIT) {
                        Column {
                            GameView(Modifier.weight(gameGridWeight))
                            SHowPortraitAds(Modifier.fillMaxWidth()
                                .weight(10.0f - gameGridWeight))
                        }
                    } else {
                        Row {
                            GameView(Modifier.weight(1f))
                            ShowLandscapeAds(modifier = Modifier.weight(1f))
                        }
                    }
                    // Top10PlayerUI()
                    // SettingUI()
                    Box {
                        SaveGameDialog()
                        LoadGameDialog()
                        SaveScoreDialog()
                    }
                }
            }
            LaunchedEffect(Unit) {
                Log.d(mTAG, "onCreate.setContent.LaunchedEffect")
                viewModel.setWhichGame(whichGame)
                viewModel.initGame(savedInstanceState)
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(mTAG, "onBackPressedDispatcher.handleOnBackPressed")
                onBackWasPressed()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d(mTAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(mTAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(mTAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(mTAG, "onStop")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(mTAG, "onConfigurationChanged.newConfig.orientation = " +
                "${newConfig.orientation}")
        mOrientation.intValue = newConfig.orientation
        getScreenSize()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(mTAG, "onSaveInstanceState")
        if (isChangingConfigurations) {
            // configuration is changing then remove top10Fragment
            ColorBallsApp.isShowingLoadingMessage = false
            ColorBallsApp.isProcessingJob = false
        }
        viewModel.onSaveInstanceState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(mTAG, "onDestroy")
    }

    private fun onBackWasPressed() {
        // capture the event of back button when it is pressed
        // change back button behavior
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            viewModel.quitGame() //   from   END PROGRAM
        } else {
            exitAppTimer.start()
            val toastFontSize = textFontSize * 0.7f
            Log.d(mTAG, "toastFontSize = $toastFontSize")
            ScreenUtil.showToast(
                this@ColorBallActivity,
                getString(R.string.backKeyToExitApp),
                toastFontSize,
                ScreenUtil.FontSize_Pixel_Type,
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun getScreenSize() {
        val screen = ScreenUtil.getScreenSize(this)
        Log.d(mTAG, "getScreenSize.screen.x = ${screen.x}")
        Log.d(mTAG, "getScreenSize.screen.y = ${screen.y}")
        screenX = screen.x.toFloat()
        screenY = screen.y.toFloat()
    }

    private fun showColorWhenClick(isClicked: MutableState<Boolean>) {
        CoroutineScope(Dispatchers.Default).launch {
            isClicked.value = true
            delay(500)
            isClicked.value = false
        }
    }

    /*
    @Composable
    fun Top10PlayerUI() {
        Log.d(mTAG, "Top10PlayerUI.mOrientation.intValue " +
                "= ${mOrientation.intValue}")
        val title = viewModel.getTop10TitleName()
        if (title.isNotEmpty()) {
            var isDialogOpen by remember { mutableStateOf(true) }
            if (isDialogOpen) {
                Dialog(onDismissRequest = { isDialogOpen = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    content = {
                        Composables.Top10Composable(
                            title = title,
                            topPlayers = viewModel.getTop10Players(), buttonListener =
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
    }

    @Composable
    fun SettingUI() {
        Log.d(mTAG, "SettingUI.mOrientation.intValue " +
                "= ${mOrientation.intValue}")
        if (viewModel.getSettingTitle().isNotEmpty()) {
            var isDialogOpen by remember { mutableStateOf(true) }
            val setting = Settings(viewModel.hasSound(),
                viewModel.isEasyLevel(), viewModel.hasNextBall())
            val textClick = object : Composables.SettingClickListener {
                override fun hasSoundClick(hasSound: Boolean) {
                    Log.d(mTAG, "textClick.hasSoundClick.hasSound = $hasSound")
                    setting.hasSound = hasSound
                }
                override fun easyLevelClick(easyLevel: Boolean) {
                    Log.d(mTAG, "textClick.easyLevelClick.easyLevel = $easyLevel")
                    setting.easyLevel = easyLevel
                }
                override fun hasNextClick(hasNext: Boolean) {
                    Log.d(mTAG, "textClick.hasNextClick.hasNext = $hasNext")
                    setting.hasNextBall = hasNext
                }
            }

            val buttonClick = object : Composables.ButtonClickListener  {
                override fun buttonOkClick() {
                    viewModel.setSettingTitle("")
                    isDialogOpen = false
                    viewModel.setHasSound(setting.hasSound)
                    viewModel.setEasyLevel(setting.easyLevel)
                    viewModel.setHasNextBall(setting.hasNextBall, true)
                    // showInterstitialAd()
                }
                override fun buttonCancelClick() {
                    isDialogOpen = false
                    viewModel.setSettingTitle("")
                    // showInterstitialAd()
                }
            }
            if (isDialogOpen) {
                Dialog(onDismissRequest = { isDialogOpen = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    content = {
                        Composables.SettingCompose(
                            this@ColorBallActivity,
                            buttonClick, textClick, getString(R.string.settingStr),
                            backgroundColor = Color(0xbb0000ff), setting
                        )
                    })
            }
        }
    }
    */

    @Composable
    fun GameView(modifier: Modifier) {
        Log.d(mTAG, "GameView.mOrientation.intValue = ${mOrientation.intValue}")
        Log.d(mTAG, "GameView.screenX = $screenX, screenY = $screenY")
        var maxWidth = screenX
        // val barWeight = 10.0f - gameGridWeight
        val barWeight = 1.0f
        val gameWeight = gameGridWeight
        if (mOrientation.intValue == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(mTAG, "GameView.ORIENTATION_LANDSCAPE")
            maxWidth = screenX/2.0f
            // barWeight = 1.0f
            // gameWeight = 7.0f
        }
        val gridHeight = ScreenUtil.pixelToDp(screenY) * gameWeight / 10.0f
        Log.d(mTAG, "GameView.gridHeight = $gridHeight")
        val heightPerBall = gridHeight / Constants.ROW_COUNTS
        Log.d(mTAG, "GameView.heightPerBall = $heightPerBall")
        val widthPerBall = ScreenUtil.pixelToDp(maxWidth) / Constants.COLUMN_COUNTS
        Log.d(mTAG, "GameView.widthPerBall = $widthPerBall")
        // set size of color balls
        mImageSizeDp = if (heightPerBall>widthPerBall) widthPerBall
        else heightPerBall
        Log.d(mTAG, "GameView.mImageSizeDp = $mImageSizeDp")
        Log.d(mTAG, "GameView.mImageSizeDp*Constants.COLUMNS " +
                "= ${mImageSizeDp*Constants.COLUMN_COUNTS}")
        val sizePx = ScreenUtil.dpToPixel(mImageSizeDp)
        bitmapDrawableResources(sizePx)

        val topPadding = 0f
        // val backgroundColor = Color(getColor(R.color.yellow3))
        Column(modifier = modifier.fillMaxHeight()) {
            ToolBarMenu(modifier = Modifier.weight(barWeight)
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
        Log.d(mTAG, "ToolBarMenu.mOrientation.intValue" +
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
        Log.d(mTAG, "ShowCurrentScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = viewModel.getCurrentScore().toString(),
            modifier = modifier,
            color = Color.Red, fontSize = Composables.mFontSize
        )
    }

    @Composable
    fun SHowHighestScore(modifier: Modifier) {
        Log.d(mTAG, "SHowHighestScore.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Text(text = viewModel.getHighestScore().toString(),
            modifier = modifier,
            color = Color.White, fontSize = Composables.mFontSize
        )
    }

    @Composable
    fun UndoButton(modifier: Modifier) {
        Log.d(mTAG, "UndoButton.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val isClicked = remember { mutableStateOf(false) }
            IconButton (onClick = {
                showColorWhenClick(isClicked)
                viewModel.undoTheLast()
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

    private fun onClickSettingButton() {
        Intent(
            this@ColorBallActivity,
            CbSettingActivity::class.java
        ).let {
            Bundle().apply {
                putBoolean(Constants.HAS_SOUND, viewModel.hasSound())
                putBoolean(Constants.IS_EASY_LEVEL, viewModel.isEasyLevel())
                putBoolean(Constants.HAS_NEXT_BALL, viewModel.hasNextBall())
                it.putExtras(this)
                settingLauncher.launch(it)
            }
        }
    }

    @Composable
    fun SettingButton(modifier: Modifier) {
        Log.d(mTAG, "SettingButton.mOrientation.intValue" +
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

    private fun showTop10Players(isLocal: Boolean) {
        Log.d(mTAG, "showTop10Players.isLocal = $isLocal")
        Intent(
            this@ColorBallActivity,
            Top10Activity::class.java
        ).let {
            Bundle().apply {
                putBoolean(Constants.IS_LOCAL_TOP10, isLocal)
                it.putExtras(this)
                top10Launcher.launch(it)
            }
        }
    }

    @Composable
    fun ShowMenu(modifier: Modifier) {
        Log.d(mTAG, "ShowMenu.mOrientation.intValue" +
                 " = ${mOrientation.intValue}")
        val dropdownWidth =
            if (mOrientation.intValue == Configuration.ORIENTATION_PORTRAIT) {
                mImageSizeDp * 6.0f
            } else {
                mImageSizeDp * 8.0f
            }
        var expanded by remember { mutableStateOf(false) }
        Log.d(mTAG, "ShowMenu.expanded = $expanded")
        Column(modifier = modifier) {
            IconButton (onClick = { expanded = !expanded }, modifier = modifier) {
                Icon(
                    painter = painterResource(R.drawable.three_dots),
                    contentDescription = "",
                    tint = if (expanded) Color.Red else Color.White
                )
            }
            DropdownMenu(expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.requiredHeightIn(max = (mImageSizeDp*12f).dp)
                    .requiredWidth(dropdownWidth.dp)
                    .background(color =
                Color(getColor(android.R.color.holo_green_light)))
                    .padding(all = 0.dp),
            ) {
                val isGlobalTop10Clicked = remember { mutableStateOf(false) }
                Composables.DropdownMenuItem(
                    text = getString(R.string.globalTop10Str),
                    color = if (isGlobalTop10Clicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isGlobalTop10Clicked)
                        showTop10Players(isLocal = false)
                    })

                val isLocalTop10Clicked = remember { mutableStateOf(false) }
                Composables.DropdownMenuItem(
                    text = getString(R.string.localTop10Score),
                    color = if (isLocalTop10Clicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isLocalTop10Clicked)
                        showTop10Players(isLocal = true)
                    })

                val isSaveGameClicked = remember { mutableStateOf(false) }
                Composables.DropdownMenuItem(
                    text = getString(R.string.saveGameStr),
                    color = if (isSaveGameClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isSaveGameClicked)
                        viewModel.saveGame()
                    })

                val isLoadGameClicked = remember { mutableStateOf(false) }
                Composables.DropdownMenuItem(
                    text = getString(R.string.loadGameStr),
                    color = if (isLoadGameClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isLoadGameClicked)
                        viewModel.loadGame()
                    })

                val isNewGameClicked = remember { mutableStateOf(false) }
                Composables.DropdownMenuItem(
                    text = getString(R.string.newGame),
                    color = if (isNewGameClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isNewGameClicked)
                        viewModel.newGame()
                    })

                val isPrivacyClicked = remember { mutableStateOf(false) }
                Composables.DropdownMenuItem(
                    text = getString(R.string.privacyPolicyString),
                    color = if (isPrivacyClicked.value) Color.Red else Color.Black,
                    onClick = {
                        expanded = false
                        showColorWhenClick(isPrivacyClicked)
                        PrivacyPolicyUtil.startPrivacyPolicyActivity(
                            this@ColorBallActivity, 10)
                              },
                    isDivider = false)
            }
        }
    }

    @Composable
    fun GameViewGrid(modifier: Modifier = Modifier) {
        Log.d(mTAG, "GameViewGrid.mOrientation.intValue" +
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
        Log.d(mTAG, "ShowGameGrid.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Log.d(mTAG, "ShowGameGrid.mImageSizeDp = $mImageSizeDp")
        boxImage?.let {
            Log.d(mTAG, "ShowGameGrid.boxImage.width = ${it.width}")
            Log.d(mTAG, "ShowGameGrid.boxImage.height = ${it.height}")
        }
        Column {
            for (i in 0 until Constants.ROW_COUNTS) {
                Row {
                    for (j in 0 until Constants.ROW_COUNTS) {
                        Box(Modifier
                            .clickable {
                                viewModel.cellClickListener(i, j)
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
        Log.d(mTAG, "ShowMessageOnScreen.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val message = viewModel.getScreenMessage()
        if (message.isEmpty()) return
        val gameViewLength = mImageSizeDp * Constants.COLUMN_COUNTS.toFloat()
        val width = (gameViewLength/2f).dp
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
    }

    @Composable
    fun ShowColorBall(i: Int, j: Int) {
        Log.d(mTAG, "ShowColorBall.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val ballInfo = viewModel.gridDataArray[i][j].value
        val ballColor = ballInfo.ballColor
        Log.d(mTAG, "ShowColorBall.ballColor = $ballColor")
        val isAnimation = ballInfo.isAnimation
        Log.d(mTAG, "ShowColorBall.isAnimation = $isAnimation")
        val isReSize = ballInfo.isResize
        Log.d(mTAG, "ShowColorBall.isReSize = $isReSize")
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
                contentScale = scale,
                modifier = modifier
            )
        }
    }

    @Composable
    fun SHowPortraitAds(modifier: Modifier) {
        Log.d(mTAG, "SHowPortraitAds.mOrientation.intValue" +
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
            ShowAdmobNormalBanner(modifier = Modifier.weight(1.0f))
            // ShowAdmobAdaptiveBanner(modifier = Modifier.weight(1.0f), 0)
            ShowFacebookBanner(modifier = Modifier.weight(1.0f),
                ColorBallsApp.facebookBannerID)
            // ShowFacebookBanner(modifier = Modifier.weight(1.0f),
            //     ColorBallsApp.facebookBannerID2)
        }
    }

    @Composable
    fun ShowNativeAd(modifier: Modifier = Modifier) {
        Log.d(mTAG, "ShowNativeAd.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
        LaunchedEffect(Unit) {
            object : GoogleNativeAd(this@ColorBallActivity,
                ColorBallsApp.googleAdMobNativeID) {
                override fun setNativeAd(ad: NativeAd?) {
                    Log.d(mTAG, "ShowNativeAd.GoogleNativeAd.setNativeAd")
                    nativeAd = ad
                }
            }
        }   // end of LaunchedEffect
        nativeAd?.let {
            MyNativeAdView(modifier = modifier, ad = it) { ad, view ->
                // head Column
                Column(modifier = modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    // head Row
                    Row(
                        modifier = Modifier.weight(8.0f).fillMaxWidth(),
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
                        Log.d(mTAG, "ShowNativeAd.callToAction.cta = $cta")
                        Column(modifier = Modifier.weight(2.0f).fillMaxWidth(),
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
                            ) { Text(text = cta, fontSize = Composables.mFontSize) }
                        }
                    }   // end of ad.callToAction */
                }   // end of head Column
            }   // end of MyNativeAdView
        }
    }

    @Composable
    fun ShowLandscapeAds(modifier: Modifier) {
        Log.d(mTAG, "ShowLandscapeAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        val colHeight = with(LocalDensity.current) {
            screenY.toDp()
        }
        Column(modifier = modifier.height(height = colHeight)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            ShowNativeAd(modifier = Modifier.weight(8.0f))
            ShowAdmobNormalBanner(modifier = Modifier.weight(2.0f))
            /*
            ShowFacebookBanner(modifier = Modifier.weight(2.0f)
                .padding(top = 10.dp),
                ColorBallsApp.facebookBannerID2)
            */
        }
    }
}
