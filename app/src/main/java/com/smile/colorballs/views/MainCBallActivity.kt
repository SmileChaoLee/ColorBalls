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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.ballsremover.views.BallsRemoverActivity
import com.smile.colorballs.smileapps.SmileAppsActivity
import com.smile.colorballs.tools.LogUtil
import com.smile.colorballs.views.ui.theme.ColorBallsTheme
import com.smile.colorballs.views.ui.theme.Yellow3
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class MainCBallActivity : ComponentActivity() {

    private var screenSize = Point(0, 0)
    // the following are for ColorBallActivity
    private lateinit var cBallLauncher: ActivityResultLauncher<Intent>
    // the following are for BarrierCBallActivity
    private lateinit var barrierCBLauncher: ActivityResultLauncher<Intent>
    // the following are for BallsRemoverActivity
    private lateinit var ballsRemoverLauncher: ActivityResultLauncher<Intent>
    private lateinit var smileAppsLauncher: ActivityResultLauncher<Intent>
    //
    private val loadingMessage = mutableStateOf("")
    private val backgroundColor = Yellow3

    @SuppressLint("ConfigurationScreenWidthHeight", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screenSize = ScreenUtil.getScreenSize(this@MainCBallActivity)

        ColorBallsApp.textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@MainCBallActivity)
        val toastTextSize = ColorBallsApp.textFontSize * 0.7f
        CbComposable.mFontSize = ScreenUtil.pixelToDp(ColorBallsApp.textFontSize).sp
        CbComposable.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp

        cBallLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "cBallLauncher.result received")
            loadingMessage.value = ""
        }

        barrierCBLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "barrierCBLauncher.result received")
            loadingMessage.value = ""
        }

        ballsRemoverLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "ballsRemoverLauncher.result received")
            loadingMessage.value = ""
        }

        smileAppsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            LogUtil.i(TAG, "smileAppsLauncher.result received")
            loadingMessage.value = ""
        }

        // enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            LogUtil.d(TAG,"onCreate.setContent")
            ColorBallsTheme {
                Scaffold { innerPadding ->
                    Box(Modifier.padding(innerPadding)
                            .background(color = backgroundColor)) {
                        DisplayLoading()
                        CreateMainUI()
                    }
                }
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d(TAG, "handleOnBackPressed")
                    exitApp()
                }
            })
        }
    }

    private fun exitApp() {
        LogUtil.i(TAG, "exitApp")
        finish()
    }

    private fun startColorBallActivity() {
        Intent(
            this@MainCBallActivity,
            ColorBallActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            cBallLauncher.launch(it)
        }
    }

    private fun startBarrierCBallActivity() {
        Intent(
            this@MainCBallActivity,
            BarrierCBallActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            barrierCBLauncher.launch(it)
        }
    }

    private fun startBallsRemoverActivity() {
        Intent(
            this@MainCBallActivity,
            BallsRemoverActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            ballsRemoverLauncher.launch(it)
        }
    }

    private fun showSmileAppsActivity() {
        Intent(
            this@MainCBallActivity,
            SmileAppsActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            smileAppsLauncher.launch(it)
        }
    }

    @Composable
    fun DisplayLoading() {
        if (loadingMessage.value.isEmpty()) {
            return
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = getString(R.string.loadingStr),
                color = Color.Blue, fontWeight = FontWeight.Bold,
                fontSize = CbComposable.mFontSize.times(2.0f))
        }
    }

    @Composable
    fun NoBarrierCBallButton(modifier: Modifier = Modifier,
                             buttonWidth: Float,
                             buttonHeight: Float,
                             textLineHeight: TextUnit) {
        LogUtil.d(TAG, "NoBarrierCBallButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val noBarrierClicked = remember { mutableStateOf(false) }
            Button(
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
            { Text(text = getString(R.string.noBarrierColorBall),
                fontSize = CbComposable.mFontSize) }
        }
    }

    @Composable
    fun BarrierCBallButton(modifier: Modifier = Modifier,
                           buttonWidth: Float,
                           buttonHeight: Float,
                           textLineHeight: TextUnit) {
        LogUtil.d(TAG, "BarrierCBallButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val barrierClicked = remember { mutableStateOf(false) }
            Button(
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
            { Text(text = getString(R.string.barrierColorBall),
                fontSize = CbComposable.mFontSize) }
        }
    }

    @Composable
    fun BallsRemoverButton(modifier: Modifier = Modifier,
                             buttonWidth: Float,
                             buttonHeight: Float,
                             textLineHeight: TextUnit) {
        LogUtil.d(TAG, "BallsRemoverButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val bRemoverClicked = remember { mutableStateOf(false) }
            Button(
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
            { Text(text = getString(R.string.removeBalls),
                fontSize = CbComposable.mFontSize) }
        }
    }

    @Composable
    fun SmileAppsButton(modifier: Modifier = Modifier,
                           buttonWidth: Float,
                           buttonHeight: Float,
                           textLineHeight: TextUnit) {
        LogUtil.d(TAG, "SmileAppsButton")
        val buttonBackground = Color.Transparent
        val buttonContentColor = Color.Green
        val buttonContainerColor = Color.Blue
        Column(modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            val isClicked = remember { mutableStateOf(false) }
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        isClicked.value = true
                        delay(200)
                        showSmileAppsActivity()
                        isClicked.value = false
                    }
                },
                modifier = Modifier//.weight(1.0f)
                    .width(width = buttonWidth.dp)
                    .height(height = buttonHeight.dp)
                    .background(color = buttonBackground),
                colors = ButtonColors(
                    containerColor =
                        if (!isClicked.value) buttonContainerColor
                        else Color.Cyan,
                    disabledContainerColor = buttonContainerColor,
                    contentColor =
                        if (!isClicked.value)
                            buttonContentColor
                        else Color.Red ,
                    disabledContentColor = buttonContentColor
                )
            )
            { Text(text = getString(R.string.smileApps),
                fontSize = CbComposable.mFontSize) }
        }
    }

    /*
    @Composable
    fun CreateMainUI() {
        LogUtil.d(TAG, "CreateMainUI")
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
        LogUtil.d(TAG, "CreateMainUI.buttonWidth = $buttonWidth")
        // 1 in 5
        val buttonHeight = maxHeight * ((10.0f - verSpacerWeight * 2.0f) / 10.0f) / 5.0f
        LogUtil.d(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val textLineHeight = (CbComposable.toastFontSize.value + 5.0f).sp
        Column(modifier = Modifier
            .fillMaxSize()) {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .weight(verSpacerWeight))
            Row(modifier = Modifier.weight(10.0f - verSpacerWeight * 2.0f)) {
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .weight(horSpacerWeight))
                Column(modifier = Modifier
                    .weight(10.0f - horSpacerWeight * 2.0f)) {
                    NoBarrierCBallButton(modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight)
                    BarrierCBallButton(modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight)
                    BallsRemoverButton(modifier = Modifier.weight(1.0f),
                        buttonWidth, buttonHeight, textLineHeight)
                }
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .weight(horSpacerWeight))
            }
            Spacer(modifier = Modifier
                .fillMaxSize()
                .weight(verSpacerWeight))
        }
    }
    */

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
            NoBarrierCBallButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
            BarrierCBallButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
            BallsRemoverButton(modifier = Modifier.weight(1.0f),
                buttonWidth, buttonHeight, textLineHeight)
            SmileAppsButton(modifier = Modifier.weight(1.0f),
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
    }

    companion object {
        private const val TAG = "MainCBallActivity"
    }
}