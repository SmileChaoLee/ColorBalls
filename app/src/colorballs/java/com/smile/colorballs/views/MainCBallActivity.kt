package com.smile.colorballs.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.smile.colorballs_main.R
import com.google.android.ump.ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
import com.smile.ballsremover.views.BallsRemoverActivity
import com.smile.colorballs_main.BaseApp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.CbComposable
import com.smile.smilelibraries.utilities.UmpUtil
import com.smile.colorballs_main.views.ui.theme.ColorBallsTheme
import com.smile.colorballs_main.views.ui.theme.Yellow3
import com.smile.dropcolorballs.views.DropCBallsActivity
import com.smile.reversi.views.MainRevActivity
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainCBallActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainCBallActivity"
    }

    private var textFontSize = 0f
    private var toastTextSize = 0f
    private var screenSize = Point(0, 0)
    // the following are for ColorBallActivity
    private lateinit var cBallLauncher: ActivityResultLauncher<Intent>
    private lateinit var barrierCBLauncher: ActivityResultLauncher<Intent>
    private lateinit var ballsRemoverLauncher: ActivityResultLauncher<Intent>
    private lateinit var dropCBallsLauncher: ActivityResultLauncher<Intent>
    private lateinit var reversiLauncher: ActivityResultLauncher<Intent>
    //
    private val loadingMessage = mutableStateOf("")
    private val backgroundColor = Yellow3
    private val buttonBackground = Color.Transparent
    private val buttonContentColor = Color.Green
    private val buttonContainerColor = Color.Blue
    private var isBackPressedEnabled = true

    private var isNoBarrierEnabled by mutableStateOf(true)
    private var isBarrierEnabled by mutableStateOf(true)
    private var isBallsRemEnabled by mutableStateOf(true)
    private var isDropCBallsEnabled by mutableStateOf(true)
    private var isReversiEnabled by mutableStateOf(true)
    private var interstitialAd: ShowInterstitial? = null

    @SuppressLint("ConfigurationScreenWidthHeight",
        "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@MainCBallActivity)
        toastTextSize = textFontSize * 0.7f
        CbComposable.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp
        CbComposable.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp
        screenSize = ScreenUtil.getScreenSize(this@MainCBallActivity)

        LogUtil.d(TAG, "onCreate.interstitialAd")
        val mBaseApp = application as? BaseApp
        interstitialAd = ShowInterstitial(this, null,
            mBaseApp?.getInterstitial())

        super.onCreate(savedInstanceState)

        cBallLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "cBallLauncher.result = $result")
            loadingMessage.value = ""
            showInterstitialAd()
            enableMainButtons()
        }
        barrierCBLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "barrierCBLauncher.result = $result")
            loadingMessage.value = ""
            showInterstitialAd()
            enableMainButtons()
        }
        ballsRemoverLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "ballsRemoverLauncher.result = $result")
            loadingMessage.value = ""
            enableMainButtons()
        }
        dropCBallsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "dropCBallsLauncher.result = $result")
            loadingMessage.value = ""
            enableMainButtons()
        }
        reversiLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.d(TAG, "reversiLauncher.result = $result")
            loadingMessage.value = ""
            enableMainButtons()
        }

        disableExitApp()
        disableMainButtons()
        // enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LogUtil.d(TAG,"onCreate.setContent")
            ColorBallsTheme {
                Scaffold { innerPadding ->
                    Box(
                        Modifier.padding(innerPadding)
                            .background(color = backgroundColor)
                    ) {
                        CbComposable.DisplayLoading(
                            loadingMessage,
                            backgroundColor,
                            getString(R.string.loadingStr)
                        )
                        CreateMainUI()
                    }
                }
            }
            LaunchedEffect(Unit) {
                dataConsentRequest()
            }
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "handleOnBackPressed")
                    exitApp()
                }
            })
    }

    private fun enableExitApp() {
        isBackPressedEnabled = true
    }

    private fun disableExitApp() {
        isBackPressedEnabled = false
    }

    private fun exitApp() {
        LogUtil.d(TAG, "exitApp.isBackPressedEnabled = $isBackPressedEnabled")
        if (isBackPressedEnabled) finish()
    }

    private fun enableMainButtons() {
        isNoBarrierEnabled = true
        isBarrierEnabled = true
        isBallsRemEnabled = true
        isDropCBallsEnabled = true
        isReversiEnabled = true
    }

    private fun disableMainButtons() {
        isNoBarrierEnabled = false
        isBarrierEnabled = false
        isBallsRemEnabled = false
        isDropCBallsEnabled = false
        isReversiEnabled = false
    }

    private fun showInterstitialAd() {
        LogUtil.d(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    private fun dataConsentRequest() {
        LogUtil.d(TAG, "dataConsentRequest")
        // setTestDeviceIds(Arrays.asList("0FFD34B018082E4BCF218FE6299B48A2"))
        // val deviceHashedId = "0FFD34B018082E4BCF218FE6299B48A2" // for debug test
        val deviceHashedId = "" // for release
        UmpUtil.initConsentInformation(this@MainCBallActivity,
            DEBUG_GEOGRAPHY_EEA,deviceHashedId,
            object : UmpUtil.UmpInterface {
                override fun callback() {
                    LogUtil.d(TAG, "dataConsentRequest.finished")
                    enableMainButtons()
                    enableExitApp()
                }
            })
    }

    private fun startColorBallActivity() {
        Intent(
            this@MainCBallActivity,
            ColorBallActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            cBallLauncher.launch(it)
        }
    }

    private fun startBarrierCBallActivity() {
        Intent(
            this@MainCBallActivity,
            BarrierCBallActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            barrierCBLauncher.launch(it)
        }
    }

    private fun startBallsRemoverActivity() {
        Intent(
            this@MainCBallActivity,
            BallsRemoverActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            barrierCBLauncher.launch(it)
        }
        // AppLinkUtil.startAppLinkOnStore(this@MainCBallActivity,
        //     AppLinkUtil.BALLS_REMOVER_LINK)
    }

    private fun startDropCBallsActivity() {
        Intent(
            this@MainCBallActivity,
            DropCBallsActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            barrierCBLauncher.launch(it)
        }
        // AppLinkUtil.startAppLinkOnStore(this@MainCBallActivity,
        //     AppLinkUtil.DROP_COLOR_BALLS_LINK)
    }

    private fun startMainRevActivity() {
        Intent(
            this@MainCBallActivity,
            MainRevActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            barrierCBLauncher.launch(it)
        }
    }

    @Composable
    fun NoBarrierCBallButton(modifier: Modifier = Modifier,
                             buttonWidth: Float,
                             buttonHeight: Float,
                             textLineHeight: TextUnit) {
        LogUtil.d(TAG, "NoBarrierCBallButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val noBarrierClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isNoBarrierEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        noBarrierClicked.value = true
                        delay(200)
                        startColorBallActivity()
                        noBarrierClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!noBarrierClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!noBarrierClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.noBarrierColorBall),
                    lineHeight = textLineHeight,
                    fontSize = CbComposable.mFontSize
                )
            }
        }
    }

    @Composable
    fun BarrierCBallButton(modifier: Modifier = Modifier,
                           buttonWidth: Float,
                           buttonHeight: Float,
                           textLineHeight: TextUnit) {
        LogUtil.d(TAG, "BarrierCBallButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val barrierClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isBarrierEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        barrierClicked.value = true
                        delay(200)
                        startBarrierCBallActivity()
                        barrierClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!barrierClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!barrierClicked.value)
                            buttonContentColor
                        else Color.Red,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.barrierColorBall),
                    lineHeight = textLineHeight,
                    fontSize = CbComposable.mFontSize
                )
            }
        }
    }

    @Composable
    fun BallsRemoverButton(modifier: Modifier = Modifier,
                             buttonWidth: Float,
                             buttonHeight: Float,
                             textLineHeight: TextUnit) {
        LogUtil.d(TAG, "BallsRemoverButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val bRemoverClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isBallsRemEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        bRemoverClicked.value = true
                        delay(200)
                        startBallsRemoverActivity()
                        bRemoverClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!bRemoverClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!bRemoverClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.balls_remover_name),
                    lineHeight = textLineHeight,
                    fontSize = CbComposable.mFontSize
                )
            }
        }
    }

    @Composable
    fun DropCBallsButton(modifier: Modifier = Modifier,
                           buttonWidth: Float,
                           buttonHeight: Float,
                           textLineHeight: TextUnit) {
        LogUtil.d(TAG, "DropCBallsButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val isFcbClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isDropCBallsEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        isFcbClicked.value = true
                        delay(200)
                        startDropCBallsActivity()
                        isFcbClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!isFcbClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!isFcbClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.drop_cballs_name),
                    lineHeight = textLineHeight,
                    fontSize = CbComposable.mFontSize
                )
            }
        }
    }

    @Composable
    fun ReversiButton(modifier: Modifier = Modifier,
                         buttonWidth: Float,
                         buttonHeight: Float,
                         textLineHeight: TextUnit) {
        LogUtil.d(TAG, "ReversiButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val isFcbClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isReversiEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        isFcbClicked.value = true
                        delay(200)
                        startMainRevActivity()
                        isFcbClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!isFcbClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!isFcbClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            {
                Text(
                    text = getString(R.string.reversi_name),
                    lineHeight = textLineHeight,
                    fontSize = CbComposable.mFontSize
                )
            }
        }
    }

    @Composable
    fun CreateMainUI() {
        LogUtil.d(TAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val maxWidth = ScreenUtil.pixelToDp(screenSize.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screenSize.y.toFloat())
        LogUtil.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
        var verSpacerWeight = 1.0f
        var horSpacerWeight = 1.0f
        /*
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            verSpacerWeight = 0.2f
            horSpacerWeight = 2.5f
        }
        */
        var buttonWidth = maxWidth * ((10.0f - horSpacerWeight * 2.0f) / 10.0f)
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            buttonWidth /= 2.0f
        }
        LogUtil.d(TAG, "CreateMainUI.buttonWidth = $buttonWidth")
        // 1 in 5
        val buttonHeight = maxHeight * ((10.0f - verSpacerWeight * 2.0f) / 10.0f) / 5.0f
        LogUtil.d(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val textLineHeight = (CbComposable.toastFontSize.value + 5.0f).sp
        val orientation = resources.configuration.orientation
        LogUtil.d(TAG, "CreateMainUI.orientation = $orientation")
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NoBarrierCBallButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
                BarrierCBallButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
                BallsRemoverButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
                DropCBallsButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
                ReversiButton(
                    modifier = Modifier.weight(1.0f),
                    buttonWidth, buttonHeight, textLineHeight
                )
            }
        } else {
            // Landscape orientation
            Row(modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NoBarrierCBallButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                    BarrierCBallButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                    BallsRemoverButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DropCBallsButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                    ReversiButton(
                        modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        LogUtil.d(TAG, "onSaveInstanceState()")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
        interstitialAd?.releaseInterstitial()
    }
}