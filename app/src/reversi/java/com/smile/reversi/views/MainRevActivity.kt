package com.smile.reversi.views

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
import com.smile.colorballs_main.BaseApp
import com.smile.colorballs_main.tools.LogUtil
import com.smile.colorballs_main.views.CbComposable
import com.smile.smilelibraries.utilities.UmpUtil
import com.smile.colorballs_main.views.ui.theme.ColorBallsTheme
import com.smile.colorballs_main.views.ui.theme.Yellow3
import com.smile.reversi.constants.ReversiConstants
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainRevActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainRevActivity"
    }

    private var textFontSize = 0f
    private var toastTextSize = 0f
    private var screenSize = Point(0, 0)
    // the following are for ColorBallActivity
    private lateinit var playWithAiLauncher: ActivityResultLauncher<Intent>
    private lateinit var twoPlayersLauncher: ActivityResultLauncher<Intent>
    //
    private val loadingMessage = mutableStateOf("")
    private val backgroundColor = Yellow3
    private val buttonBackground = Color.Transparent
    private val buttonContentColor = Color.Green
    private val buttonContainerColor = Color.Blue
    private var isBackPressedEnabled = true

    private var isPlayWithAiEnabled by mutableStateOf(true)
    private var isTwoPlayersEnabled by mutableStateOf(true)
    private var interstitialAd: ShowInterstitial? = null

    @SuppressLint("ConfigurationScreenWidthHeight",
        "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@MainRevActivity)
        toastTextSize = textFontSize * 0.7f
        CbComposable.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp
        CbComposable.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp
        screenSize = ScreenUtil.getScreenSize(this@MainRevActivity)

        LogUtil.d(TAG, "onCreate.interstitialAd")
        val mBaseApp = application as? BaseApp
        interstitialAd = ShowInterstitial(this, null,
            mBaseApp?.getInterstitial())

        super.onCreate(savedInstanceState)

        playWithAiLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "playWithAiLauncher.result = $result")
            loadingMessage.value = ""
            showInterstitialAd()
            enableMainButtons()
        }

        twoPlayersLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "twoPlayersLauncher.result = $result")
            loadingMessage.value = ""
            showInterstitialAd()
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
        LogUtil.i(TAG, "exitApp.isBackPressedEnabled = $isBackPressedEnabled")
        if (isBackPressedEnabled) finish()
    }

    private fun enableMainButtons() {
        isPlayWithAiEnabled = true
        isTwoPlayersEnabled = true
    }

    private fun disableMainButtons() {
        isPlayWithAiEnabled = false
        isTwoPlayersEnabled = false
    }

    private fun showInterstitialAd() {
        LogUtil.i(TAG, "showInterstitialAd = $interstitialAd")
        interstitialAd?.ShowAdThread()?.startShowAd(0) // AdMob first
    }

    private fun dataConsentRequest() {
        LogUtil.d(TAG, "dataConsentRequest")
        // setTestDeviceIds(Arrays.asList("0FFD34B018082E4BCF218FE6299B48A2"))
        // val deviceHashedId = "0FFD34B018082E4BCF218FE6299B48A2" // for debug test
        val deviceHashedId = "" // for release
        UmpUtil.initConsentInformation(this@MainRevActivity,
            DEBUG_GEOGRAPHY_EEA,deviceHashedId,
            object : UmpUtil.UmpInterface {
                override fun callback() {
                    LogUtil.d(TAG, "dataConsentRequest.finished")
                    enableMainButtons()
                    enableExitApp()
                }
            })
    }

    private fun startPlayWithAiActivity() {
        Intent(
            this@MainRevActivity,
            ReversiActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            it.putExtra(ReversiConstants.PLAY_MODE, ReversiConstants.PLAY_WIth_AI)
            playWithAiLauncher.launch(it)
        }
    }

    private fun startTwoPlayersActivity() {
        Intent(
            this@MainRevActivity,
            ReversiActivity::class.java
        ).also {
            disableMainButtons()
            loadingMessage.value = getString(R.string.loadingStr)
            it.putExtra(ReversiConstants.PLAY_MODE, ReversiConstants.TWO_PLAYERS)
            twoPlayersLauncher.launch(it)
        }
    }

    @Composable
    fun PlayWithAiButton(modifier: Modifier = Modifier,
                         buttonWidth: Float,
                         buttonHeight: Float,
                         textLineHeight: TextUnit) {
        LogUtil.d(TAG, "PlayWithAiButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val noBarrierClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isPlayWithAiEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        noBarrierClicked.value = true
                        delay(200)
                        startPlayWithAiActivity()
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
                    text = getString(R.string.playWithAi),
                    lineHeight = textLineHeight,
                    fontSize = CbComposable.mFontSize
                )
            }
        }
    }

    @Composable
    fun TwoPlayersButton(modifier: Modifier = Modifier,
                         buttonWidth: Float,
                         buttonHeight: Float,
                         textLineHeight: TextUnit) {
        LogUtil.d(TAG, "TwoPlayersButton")
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val barrierClicked = remember { mutableStateOf(false) }
            Button(
                enabled = isTwoPlayersEnabled,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        barrierClicked.value = true
                        delay(200)
                        startTwoPlayersActivity()
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
                    text = getString(R.string.twoPlayers),
                    lineHeight = textLineHeight,
                    fontSize = CbComposable.mFontSize
                )
            }
        }
    }

    @Composable
    fun CreateMainUI() {
        LogUtil.i(TAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val maxWidth = ScreenUtil.pixelToDp(screenSize.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screenSize.y.toFloat())
        LogUtil.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
        var verSpacerWeight = 1.0f
        var horSpacerWeight = 1.0f
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            verSpacerWeight = 0.2f
            horSpacerWeight = 2.5f
        }
        val buttonWidth = maxWidth * ((10.0f - horSpacerWeight * 2.0f) / 10.0f)
        LogUtil.i(TAG, "CreateMainUI.buttonWidth = $buttonWidth")
        // 1 in 5
        val buttonHeight = maxHeight * ((10.0f - verSpacerWeight * 2.0f) / 10.0f) / 5.0f
        LogUtil.i(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val textLineHeight = (CbComposable.toastFontSize.value + 5.0f).sp
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            PlayWithAiButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
            TwoPlayersButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtil.i(TAG, "onResume")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        LogUtil.i(TAG, "onSaveInstanceState()")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(TAG, "onDestroy")
        interstitialAd?.releaseInterstitial()
    }
}