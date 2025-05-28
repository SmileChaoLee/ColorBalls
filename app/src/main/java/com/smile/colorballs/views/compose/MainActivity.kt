package com.smile.colorballs.views.compose

import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.BundleCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.shared_composables.ui.theme.ColorBallsTheme
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.models.GridData
import com.smile.colorballs.presenters.PresenterCompose
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil

class MainActivity : MyViewCompose() {
    companion object {
        private const val TAG = "MainActivity"
    }
    private val screenX = mutableIntStateOf(0)
    private val screenY = mutableIntStateOf(0)

    private var interstitialAd: ShowInterstitial? = null
    private var mImageSizeDp = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate.textFontSize")
        textFontSize = ScreenUtil.suitableFontSize(
                this, ScreenUtil.getDefaultTextSizeFromTheme(this,
                    ScreenUtil.FontSize_Pixel_Type, null),
                ScreenUtil.FontSize_Pixel_Type,
                0.0f)
        Composables.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp

        val isNewGame = initPresenter(savedInstanceState)

        Log.d(TAG, "onCreate.interstitialAd")
        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, it.facebookAds,
                it.googleInterstitialAd)
        }

        Log.d(TAG, "onCreate.getScreenSize()")
        getScreenSize()

        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                CreateMainUI()
                Log.d(TAG, "onCreate.setContent.mImageSize = $mImageSizeDp")
                createGame(isNewGame)
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                onBackWasPressed()
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(
            TAG, "onConfigurationChanged.newConfig.orientation = " +
                "${newConfig.orientation}")
        getScreenSize()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        if (isChangingConfigurations) {
            // configuration is changing then remove top10Fragment
            ColorBallsApp.isShowingLoadingMessage = false
            ColorBallsApp.isProcessingJob = false
        }
        saveScoreAlertDialog?.dismiss()
        mPresenter.onSaveInstanceState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mPresenter.release()
        interstitialAd?.releaseInterstitial()
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
                this@MainActivity,
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
        screenX.intValue = screen.x
        screenY.intValue = screen.y
    }

    private fun initPresenter(state: Bundle?): Boolean {
        // restore instance state
        val isNewGame: Boolean
        var gameProp: GameProp? = null
        var gridData: GridData? = null
        state?.let {
            Log.d(TAG,"initPresenter.state not null then restore the state")
            gameProp =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GAME_PROP_TAG, GameProp::class.java)
                else it.getParcelable(Constants.GAME_PROP_TAG)
            gridData =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    BundleCompat.getParcelable(it, Constants.GRID_DATA_TAG, GridData::class.java)
                else it.getParcelable(Constants.GRID_DATA_TAG)
        }
        if (gameProp == null || gridData == null) {
            Log.d(TAG, "initPresenter.prop or grid is null, new game")
            gameProp = GameProp()
            gridData = GridData()
            isNewGame = true
        } else {
            isNewGame = false
        }

        Log.d(TAG, "initPresenter.isNewGame = $isNewGame")

        mPresenter = PresenterCompose(this@MainActivity, gameProp!!, gridData!!)

        return isNewGame
    }

    private fun createGame(isNewGame: Boolean) {
        Log.d(TAG, "CreateGame")
        saveScoreAlertDialog = null
        mPresenter.initGame(isNewGame)
    }

    private fun onClickUndoButton() {
        ScreenUtil.showToast(
            this@MainActivity, "Undo",
            textFontSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }

    private fun onClickSettingButton() {
        ScreenUtil.showToast(
            this@MainActivity, "Setting",
            textFontSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }

    private fun onClickMenuIcon() {
        ScreenUtil.showToast(
            this@MainActivity, "Menu",
            textFontSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }

    @Composable
    fun CreateMainUI() {
        Log.d(TAG, "createMainUI.screenX.intValue = ${screenX.intValue}")
        Log.d(TAG, "createMainUI.screenY.intValue = ${screenY.intValue}")
        val orientation = resources.configuration.orientation
        val maxHeight = screenY.intValue.toFloat()
        Log.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
        var maxWidth: Float
        var barHeight: Float
        var adHeight: Float
        var gHeight: Float
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "CreateMainUI.ORIENTATION_PORTRAIT")
            maxWidth = screenX.intValue.toFloat()
            barHeight = (maxHeight * 1.2f) / 10f
            adHeight = maxHeight * 0.25f
            gHeight = maxHeight - barHeight - adHeight
        } else {
            Log.d(TAG, "CreateMainUI.ORIENTATION_LANDSCAPE")
            maxWidth = screenX.intValue.toFloat() / 2f
            barHeight = (maxHeight * 1.2f) / 10f
            adHeight = maxWidth
            gHeight = maxHeight - barHeight
        }
        Log.d(TAG, "CreateMainUI.maxWidth = $maxWidth")
        Log.d(TAG, "CreateMainUI.barHeight = $barHeight")
        Log.d(TAG, "CreateMainUI.gHeight = $gHeight")
        val gameSize  = if (gHeight > maxWidth) maxWidth else gHeight
        Log.d(TAG, "CreateMainUI.gameSize = $gameSize")
        val imageSizePx = (gameSize / (Constants.ROW_COUNTS.toFloat())).toInt().toFloat()
        Log.d(TAG, "CreateMainUI.imageSizePx = $imageSizePx")
        var realGameSize = (imageSizePx * Constants.ROW_COUNTS).toInt()
        Log.d(TAG, "CreateMainUI.realGameSize = $realGameSize")
        var startPadding = ((maxWidth - realGameSize) / 2f).coerceAtLeast(0f)
        Log.d(TAG, "CreateMainUI.startPadding = $startPadding")

        // set size of color balls
        bitmapDrawableResources(imageSizePx)

        mImageSizeDp = ScreenUtil.pixelToDp(imageSizePx)
        Log.d(TAG, "CreateMainUI.mImageSizeDp = $mImageSizeDp")
        maxWidth = ScreenUtil.pixelToDp(maxWidth)
        barHeight = ScreenUtil.pixelToDp(barHeight)
        realGameSize = ScreenUtil.pixelToDp(realGameSize.toFloat()).toInt()
        startPadding = ScreenUtil.pixelToDp(startPadding)
        Log.d(TAG, "CreateMainUI.startPadding.pixelToDp = $startPadding")
        adHeight = ScreenUtil.pixelToDp(adHeight)

        val topPadding = 0f
        val backgroundColor = Color(getColor(R.color.yellow3))
        Column(modifier = Modifier.fillMaxHeight()
            .width(width = maxWidth.dp)
            .background(color = backgroundColor)) {
            ToolBarMenu(modifier = Modifier.height(height = barHeight.dp)
                .padding(top = topPadding.dp, start = 0.dp))
            CreateGameView(modifier = Modifier.height(height = realGameSize.dp)
                    .padding(top = 0.dp, start = startPadding.dp))
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Portrait
                SHowPortraitAds(
                    Modifier.height(height = adHeight.dp).fillMaxWidth())
            }
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /*
            SHowLandscapeAds(modifier = Modifier
                .fillMaxHeight().width(width = adHeight.dp)
                .padding(top = 0.dp, start = adHeight.dp, end = 0.dp))
            */
        }
    }

    @Composable
    fun ToolBarMenu(modifier: Modifier) {
        Log.d(TAG, "ToolBarMenu")
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
            ShowMenuIcon(modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically))
        }
    }

    @Composable
    fun ShowCurrentScore(modifier: Modifier) {
        Text(text = mPresenter.currentScore.intValue.toString(),
            modifier = modifier,
            color = Color.Red, fontSize = Composables.mFontSize
        )
    }


    @Composable
    fun SHowHighestScore(modifier: Modifier) {
        Text(text = mPresenter.highestScore.intValue.toString(),
            modifier = modifier,
            color = Color.White, fontSize = Composables.mFontSize
        )
    }

    @Composable
    fun UndoButton(modifier: Modifier) {
        IconButton (onClick = { onClickUndoButton() }, modifier = modifier
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

    @Composable
    fun SettingButton(modifier: Modifier) {
        IconButton (onClick = { onClickSettingButton() }, modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.setting),
                contentDescription = "",
                tint = Color.White
            )
        }
    }

    @Composable
    fun ShowMenuIcon(modifier: Modifier) {
        IconButton (onClick = { onClickMenuIcon() }, modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.three_dots),
                contentDescription = "",
                tint = Color.White
            )
        }
    }

    @Composable
    fun CreateGameView(modifier: Modifier) {
        Log.d(TAG, "CreateGameView.mImageSize = $mImageSizeDp")
        Column(modifier = modifier) {
            Box {
                ShowGameGrid()
                ShowMessageOnScreen()
            }
        }
    }

    @Composable
    fun ShowGameGrid() {
        Log.d(TAG, "ShowGameGrid.mImageSize = $mImageSizeDp")
        val width0 = (mImageSizeDp * (Constants.ROW_COUNTS.toFloat()))
        Column {
            for (i in 0 until Constants.ROW_COUNTS) {
                Row(modifier = Modifier.weight(1f).width(width = width0.dp)
                    .fillMaxWidth()) {
                    for (j in 0 until Constants.ROW_COUNTS) {
                        Box(modifier = Modifier.weight(1f)
                            .clickable {
                            mPresenter.drawBallsAndCheckListener(i, j)
                        }) {
                            Image(
                                modifier = Modifier.size(mImageSizeDp.dp),
                                painter = painterResource(id = R.drawable.box_image),
                                // painter = rememberDrawablePainter(drawable = boxImage),
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
        val ballInfo = mPresenter.gridDataArray[i][j].value
        val ballColor = ballInfo.ballColor
        Log.d(TAG, "ShowColorBall.ballColor = $ballColor")
        Log.d(TAG, "ShowColorBall.isAnimation = ${ballInfo.isAnimation}")
        if (ballColor == 0) return  // no showing ball
        val whichBall = ballInfo.whichBall
        /*
        val draw: Drawable? = when(whichBall) {
            WhichBall.BALL-> { colorBallMap[ballColor] }
            WhichBall.OVAL_BALL-> { colorOvalBallMap[ballColor] }
            WhichBall.NEXT_BALL-> { colorNextBallMap[ballColor] }
            else -> { null }    // WhichBall.NO_BALL
        }
        */
        val ratio: Float = when(whichBall) {
            WhichBall.BALL-> { 1.0f }
            WhichBall.OVAL_BALL-> { 0.7f }
            WhichBall.NEXT_BALL-> { 0.6f }
            else -> { 1.0f }    // WhichBall.NO_BALL
        }
        val drawableId = colorBallMap.getValue(ballColor)
        Column(modifier = Modifier.size(mImageSizeDp.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            val modifier = Modifier.background(color = Color.Transparent)
                .size((mImageSizeDp*ratio).dp).padding(all = 0.dp)
            val isAnimation = ballInfo.isAnimation
            Log.d(TAG, "ShowColorBall.ballInfo.isAnimation = $isAnimation")
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "",
                modifier = modifier,
                contentScale = ContentScale.FillBounds
            )
        }
    }

    @Composable
    fun SHowPortraitAds(modifier: Modifier) {
        Log.d(TAG, "SHowPortraitAds") // for portrait
        Column(modifier = modifier.background(color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = "Show portrait banner ads", fontSize = Composables.mFontSize)
        }
    }

    @Composable
    fun SHowLandscapeAds(modifier: Modifier) {
        Log.d(TAG, "SHowLandscapeAds") // for portrait
        Column(modifier = modifier
            .background(color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = "Show Native and banner ads", fontSize = Composables.mFontSize)
        }
    }

    private fun exitApplication() {
        val handlerClose = Handler(Looper.getMainLooper())
        val timeDelay = 200
        // exit application
        handlerClose.postDelayed({ this.finish() }, timeDelay.toLong())
    }

    override fun showInterstitialAd() {
        Log.d(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    // implement the abstract methods of MyViewCompose
    override fun quitOrNewGame(entryPoint: Int) {
        if (entryPoint == 0) {
            //  END PROGRAM
            exitApplication()
        } else if (entryPoint == 1) {
            //  NEW GAME
            initPresenter(null)
        }
        ColorBallsApp.isProcessingJob = false
    }

    override fun setDialogStyle(dialog: DialogInterface) {
        val dlg = dialog as AlertDialog
        (dlg.window)?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.0f) // no dim for background screen
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(R.drawable.dialog_board_image)
        }

        val nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE)
        ScreenUtil.resizeTextSize(nBtn, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        nBtn.typeface = Typeface.DEFAULT_BOLD
        nBtn.setTextColor(android.graphics.Color.RED)

        val layoutParams = nBtn.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        nBtn.layoutParams = layoutParams

        val pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE)
        ScreenUtil.resizeTextSize(pBtn, textFontSize, ScreenUtil.FontSize_Pixel_Type)
        pBtn.typeface = Typeface.DEFAULT_BOLD
        pBtn.setTextColor(android.graphics.Color.rgb(0x00, 0x64, 0x00))
        pBtn.layoutParams = layoutParams
    }
}
