package com.smile.colorballs.views.compose

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.shared_composables.ui.theme.ColorBallsTheme
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichBall
import com.smile.colorballs.models.TopPlayer
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.privacy_policy.PrivacyPolicyUtil
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : MyViewCompose() {
    companion object {
        private const val TAG = "MainActivity"
    }
    private val mOrientation = mutableIntStateOf(Configuration.ORIENTATION_PORTRAIT)
    private var screenX = 0f
    private var screenY = 0f
    private val gameGridWeight = 0.7f
    private var interstitialAd: ShowInterstitial? = null
    // the following are for Top 10 Players
    // private lateinit var top10Launcher: ActivityResultLauncher<Intent>
    private val top10Players = mutableStateOf(listOf<TopPlayer>())
    private val top10TitleName = mutableStateOf("")
    //

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
        Log.d(TAG, "onCreate.getScreenSize()")
        getScreenSize()

        /*
        top10Launcher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            Log.d(TAG, "top10Launcher.result received")
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "top10Launcher.Showing interstitial ads")
                showInterstitialAd()
            }
        }
        */

        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                mOrientation.intValue = resources.configuration.orientation
                val backgroundColor = Color(getColor(R.color.yellow3))
                Box(Modifier.background(color = backgroundColor)) {
                    if (mOrientation.intValue ==
                        Configuration.ORIENTATION_PORTRAIT) {
                        Column {
                            CreateMainUI(savedInstanceState,
                                Modifier.weight(gameGridWeight))
                            SHowPortraitAds(Modifier.fillMaxWidth()
                                .weight(1.0f - gameGridWeight))
                        }
                    } else {
                        val width = (screenX/2f).dp
                        Row {
                            CreateMainUI(savedInstanceState, Modifier.weight(1f))
                            SHowLandscapeAds(modifier = Modifier.weight(1f)
                                .fillMaxSize())
                        }
                    }
                    Top10PlayerUI()
                }
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
        screenX = screen.x.toFloat()
        screenY = screen.y.toFloat()
    }

    private fun onClickSettingButton() {
        ScreenUtil.showToast(
            this@MainActivity, "Setting",
            textFontSize,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }

    @Composable
    fun CreateMainUI(state: Bundle?, modifier: Modifier) {
        Log.d(TAG, "CreateMainUI.mOrientation = $mOrientation")
        GameView(modifier)
        Log.d(TAG, "CreateMainUI.mImageSize = $mImageSizeDp")
        LaunchedEffect(Unit) {
            Log.d(TAG, "CreateMainUI.LaunchedEffect")
            mPresenter.initGame(state)
        }
    }

    @Composable
    fun Top10PlayerUI() {
        Log.d(TAG, "Top10PlayerUI.mOrientation.intValue " +
                "= ${mOrientation.intValue}")
        if (top10TitleName.value.isNotEmpty()) {
            Composables.Top10Composable(
                title = top10TitleName.value,
                topPlayers = top10Players.value, buttonListener =
                object : Composables.OkButtonListener {
                    override fun buttonOkClick() {
                        showInterstitialAd()
                        top10TitleName.value = ""
                    }
                },
                getString(R.string.okStr)
            )
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
        var adHeight: Float
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
        adHeight = ScreenUtil.pixelToDp(adHeight)

        val topPadding = 0f
        // val backgroundColor = Color(getColor(R.color.yellow3))
        Column(modifier = modifier.fillMaxHeight()
            // .background(color = backgroundColor)
            .width(width = maxWidth.dp)) {
            ToolBarMenu(modifier = Modifier.height(height = barHeight.dp)
                .padding(top = topPadding.dp, start = 0.dp))
            GameViewGrid(modifier = Modifier.height(height = realGameSize.dp)
                    .padding(top = 0.dp, start = startPadding.dp))
            /*
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Portrait
                SHowPortraitAds(
                    Modifier.fillMaxWidth().fillMaxHeight()
                        .height(height = adHeight.dp))
            }
            */
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

    @Composable
    fun SettingButton(modifier: Modifier) {
        Log.d(TAG, "SettingButton.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        IconButton (onClick = { onClickSettingButton() }, modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.setting),
                contentDescription = "",
                tint = Color.White
            )
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
                        showTop10Players(false)
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.localTop10Score),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        showTop10Players(true)
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.saveGameStr),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        mPresenter.saveGame()
                    })

                Composables.DropdownMenuItem(
                    text = getString(R.string.loadGameStr),
                    color = Color.Black,
                    onClick = {
                        expanded = false
                        mPresenter.loadGame()
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
                            this@MainActivity, 10)
                              },
                    isDivider = false)
            }
        }
    }

    private fun showTop10Players(isLocal: Boolean) {
        Log.d(TAG, "showTop10Players.isLocal = $isLocal")
        /*
        Intent(this@MainActivity,
            Top10ActivityCompose::class.java).let {
                Bundle().apply {
                    putBoolean(Constants.IS_LOCAL_TOP10, isLocal)
                    it.putExtras(this)
                    top10Launcher.launch(it)
                }
        }
        */
        top10TitleName.value =
            if (isLocal) getString(R.string.localTop10Score) else
                getString(R.string.globalTop10Str)
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "showTop10Players.lifecycleScope")
            val players = if (isLocal) {
                PlayerRecordRest.GetLocalTop10(
                    ScoreSQLite(
                        this@MainActivity
                    )
                )
            } else {
                PlayerRecordRest.GetGlobalTop10("1")
            }
            val top10 = ArrayList<TopPlayer>()
            for (i in 0 until players.size) {
                players[i].playerName?.let { name ->
                    if (name.trim().isEmpty()) players[i].playerName = "No Name"
                } ?: run {
                    Log.d(TAG, "showTop10Players.players[i].playerName = null")
                    players[i].playerName = "No Name"
                }
                top10.add(TopPlayer(players[i], medalImageIds[i]))
            }
            top10Players.value = top10
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
        Column(modifier = modifier.background(color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = "Show portrait banner ads", fontSize = Composables.mFontSize)
        }
    }

    @Composable
    fun SHowLandscapeAds(modifier: Modifier) {
        Log.d(TAG, "SHowLandscapeAds.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Column(modifier = modifier.background(color = Color.Green),
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
            mPresenter.initGame(null)
        }
        mPresenter.setSaveScoreAlertDialogState(entryPoint, false)
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
