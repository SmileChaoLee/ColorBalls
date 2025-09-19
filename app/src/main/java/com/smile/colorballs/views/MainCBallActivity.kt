package com.smile.colorballs.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.views.ui.theme.ColorBallsTheme
import com.smile.colorballs.views.ui.theme.Yellow3
import com.smile.smilelibraries.utilities.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class MainCBallActivity : ComponentActivity() {

    private var screenSize = Point(0, 0)
    private var textFontSize = 0f
    private var toastTextSize = 0f
    private var fontSize = 0f
    // the following are for ColorBallActivity
    private lateinit var cBallLauncher: ActivityResultLauncher<Intent>
    // the following are for RandomCBallActivity
    private lateinit var randomCbLauncher: ActivityResultLauncher<Intent>
    //
    private val loadingMessage = mutableStateOf("")

    @SuppressLint("ConfigurationScreenWidthHeight", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        screenSize = ScreenUtil.getScreenSize(this@MainCBallActivity)

        val defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(this@MainCBallActivity,
            ScreenUtil.FontSize_Pixel_Type, null)
        textFontSize = ScreenUtil.suitableFontSize(this@MainCBallActivity,
            defaultTextFontSize,
            ScreenUtil.FontSize_Pixel_Type,0.0f)
        toastTextSize = textFontSize * 0.7f
        fontSize = ScreenUtil.suitableFontScale(this@MainCBallActivity,
            ScreenUtil.FontSize_Pixel_Type, 0.0f)
        ColorBallsApp.textFontSize = textFontSize
        Composables.mFontSize = ScreenUtil.pixelToDp(textFontSize).sp
        Composables.toastFontSize = ScreenUtil.pixelToDp(toastTextSize).sp

        cBallLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "cBallLauncher.result received")
            loadingMessage.value = ""
        }

        randomCbLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d(TAG, "randomCbLauncher.result received")
            loadingMessage.value = ""
        }

        setContent {
            Log.d(TAG,"onCreate.setContent")
            ColorBallsTheme {
                Box {
                    DisplayLoading()
                    CreateMainUI()
                }
            }

            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(TAG, "handleOnBackPressed")
                    exitApp()
                }
            })
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            // Add other network types if needed
            else -> false
        }
    }


    private fun exitApp() {
        Log.d(TAG, "exitApp")
        finish()
    }

    private fun startRandomCBallActivity() {
        Intent(
            this@MainCBallActivity,
            BarrierCBallActivity::class.java
        ).also {
            loadingMessage.value = getString(R.string.loadingStr)
            randomCbLauncher.launch(it)
        }
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

    @Composable
    fun DisplayLoading() {
        if (loadingMessage.value.isEmpty()) {
            return
        }
        val backgroundColor = Yellow3
        Column(modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = getString(R.string.loadingStr),
                color = Color.Blue, fontWeight = FontWeight.Bold,
                fontSize = Composables.mFontSize.times(2.0f))
        }
    }

    @Composable
    fun NoBarrierCBallButton(modifier: Modifier = Modifier,
                             buttonWidth: Float,
                             buttonHeight: Float,
                             textLineHeight: TextUnit) {
        Log.d(TAG, "NoBarrierCBallButton")
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
                fontSize = Composables.mFontSize) }
        }
    }

    @Composable
    fun BarrierCBallButton(modifier: Modifier = Modifier,
                           buttonWidth: Float,
                           buttonHeight: Float,
                           textLineHeight: TextUnit) {
        Log.d(TAG, "BarrierCBallButton")
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
                        startRandomCBallActivity()
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
                fontSize = Composables.mFontSize) }
        }
    }

    @Composable
    fun CreateMainUI() {
        Log.d(TAG, "CreateMainUI")
        if (loadingMessage.value.isNotEmpty()) return
        val maxWidth = ScreenUtil.pixelToDp(screenSize.x.toFloat())
        val maxHeight = ScreenUtil.pixelToDp(screenSize.y.toFloat())
        Log.d(TAG, "CreateMainUI.maxHeight = $maxHeight")
        var verSpacerWeight = 1.0f
        var horSpacerWeight = 1.0f
        if (resources.configuration.orientation
            == Configuration.ORIENTATION_LANDSCAPE) {
            verSpacerWeight = 0.2f
            horSpacerWeight = 2.5f
        }
        val buttonWidth = maxWidth * (10.0f - horSpacerWeight * 2.0f)
        // 1 in 5
        val buttonHeight = maxHeight * (10.0f - (verSpacerWeight * 2.0f)) / 50.0f
        Log.d(TAG, "CreateMainUI.buttonHeight = $buttonHeight")
        val backgroundColor = Yellow3
        val textLineHeight = (Composables.toastFontSize.value + 5.0f).sp
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)) {
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

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(TAG, "onSaveInstanceState()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "MainCBallActivity"
    }
}