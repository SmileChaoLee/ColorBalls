package com.smile.colorballs.compose_view

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.compose_view.Composables.textFontSize
import com.smile.colorballs.compose_view.ui.theme.ColorBallsTheme
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil

private const val TAG = "MainActivity"
private const val ROW_COUNT = 9

class MainActivity : ComponentActivity() {

    private var interstitialAd: ShowInterstitial? = null
    private var currentScore = 0
    private var highestScore = 0
    private val screenX = mutableFloatStateOf(0f)
    private val screenY = mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        val textFontSize = ScreenUtil.suitableFontSize(
                this, ScreenUtil.getDefaultTextSizeFromTheme(this,
                    ScreenUtil.FontSize_Pixel_Type, null),
                ScreenUtil.FontSize_Pixel_Type,
                0.0f)
        Composables.textFontSize = ScreenUtil.pixelToDp(textFontSize).sp

        (application as ColorBallsApp).let {
            interstitialAd = ShowInterstitial(this, it.facebookAds,
                it.googleInterstitialAd)
        }

        highestScore = getHighestScore()
        getScreenSize()

        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                CreateMainUI()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged.newConfig.orientation = " +
                "${newConfig.orientation}")
        getScreenSize()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        interstitialAd?.releaseInterstitial()
    }

    private fun getScreenSize() {
        val screen = ScreenUtil.getScreenSize(this)
        Log.d(TAG, "getScreenSize.screen.x = ${screen.x}")
        Log.d(TAG, "getScreenSize.screen.y = ${screen.y}")
        screenX.floatValue = ScreenUtil.pixelToDp(screen.x.toFloat())
        screenY.floatValue = ScreenUtil.pixelToDp(screen.y.toFloat())
    }

    private fun getHighestScore() : Int {
        Log.d(TAG, "getHighestScore")
        val scoreSQLiteDB = ScoreSQLite(this)
        val score = scoreSQLiteDB.readHighestScore()
        scoreSQLiteDB.close()
        return score
    }

    private fun onClickUndoButton() {
        ScreenUtil.showToast(
            this@MainActivity, "Undo",
            textFontSize.value,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }
    private fun onClickSettingButton() {
        ScreenUtil.showToast(
            this@MainActivity, "Setting",
            textFontSize.value,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }

    @Composable
    fun CreateMainUI() {
        Log.d(TAG, "CreateMainUI.screenX.floatValue = ${screenX.floatValue}")
        Log.d(TAG, "CreateMainUI.screenX.floatValue = ${screenY.floatValue}")
        val orientation = resources.configuration.orientation
        val maxHeight = screenY.floatValue
        val maxWidth: Float
        val barHeight: Float
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            maxWidth = screenX.floatValue
            barHeight = (maxHeight * 1.2f) / 10f
        } else {
            maxWidth = screenX.floatValue / 2f
            barHeight = (maxHeight * 1.4f) / 10f
        }

        val gHeight = maxHeight - barHeight
        Log.d(TAG, "CreateMainUI.gHeight = $gHeight")
        Log.d(TAG, "CreateMainUI.maxWidth = $maxWidth")
        val imageSize = if (gHeight > maxWidth) {
            maxWidth / (ROW_COUNT.toFloat())
        } else {
            gHeight / (ROW_COUNT.toFloat())
        }
        val gameHeight = imageSize * (ROW_COUNT.toFloat())
        // val adHeight = maxHeight - barHeight - gameHeight   // for ads
        val topPadding = 0f
        Log.d(TAG, "(maxWidth - gameHeight)" +
                "= ${maxWidth - gameHeight}")
        val startPadding = ((maxWidth - gameHeight) / 2f).coerceAtLeast(0f)
        val backgroundColor = Color(getColor(R.color.yellow3))
        Column(modifier = Modifier.fillMaxHeight()
            .width(width = maxWidth.dp)
            .background(color = backgroundColor)) {
            ToolBarMenu(modifier = Modifier.height(height = barHeight.dp)
                .padding(top = topPadding.dp, start = 0.dp))
            CreateGameView(imageSize,
                modifier = Modifier.height(height = gameHeight.dp)
                    .padding(top = 0.dp, start = startPadding.dp))
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Portrait
                SHowPortraitAds(
                    Modifier.fillMaxWidth().fillMaxHeight())
                        //.height(height = adHeight.dp))
            }
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SHowLandscapeAds(modifier = Modifier
                .fillMaxHeight().fillMaxWidth()
                .padding(top = 0.dp, start = maxWidth.dp, end = 0.dp))
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
        Text(text = currentScore.toString(), modifier = modifier,
            color = Color.Red, fontSize = textFontSize)
    }


    @Composable
    fun SHowHighestScore(modifier: Modifier) {
        Text(text = highestScore.toString(), modifier = modifier,
            color = Color.White, fontSize = textFontSize)
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
        IconButton (onClick = { onClickSettingButton() }, modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.three_dots),
                contentDescription = "",
                tint = Color.White
            )
        }
    }

    @Composable
    fun CreateGameView(imageSize: Float, modifier: Modifier) {
        Log.d(TAG, "CreateGameView")
        Column(modifier = modifier) {
            for (i in 0 until ROW_COUNT) {
                Row {
                    for (j in 0 until ROW_COUNT) {
                        Image(
                            modifier = Modifier.size(imageSize.dp).padding(all = 0.dp),
                            painter = painterResource(id = R.drawable.box_image),
                            contentDescription = "",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun SHowPortraitAds(modifier: Modifier) {
        Log.d(TAG, "SHowPortraitAds") // for portrait
        Column(modifier = modifier.background(color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = "Show portrait banner ads", fontSize = textFontSize)
        }
    }

    @Composable
    fun SHowLandscapeAds(modifier: Modifier) {
        Log.d(TAG, "SHowLandscapeAds") // for portrait
        Column(modifier = modifier
            .background(color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(text = "Show Native and banner ads", fontSize = textFontSize)
        }
    }
}
