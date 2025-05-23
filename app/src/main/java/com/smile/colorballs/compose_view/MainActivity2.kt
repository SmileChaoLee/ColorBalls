package com.smile.colorballs.compose_view

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.MyActivity
import com.smile.colorballs.R
import com.smile.colorballs.compose_view.Composables.textFontSize
import com.smile.colorballs.compose_view.MyComposeActivity.Companion
import com.smile.colorballs.compose_view.ui.theme.ColorBallsTheme
import com.smile.smilelibraries.models.ShowToastMessage
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.ScreenUtil

private const val TAG = "MainActivity2"
private const val ROW_COUNT = 9
private const val COLUMN_COUNT = 9

class MainActivity2 : ComponentActivity() {

    private var interstitialAd: ShowInterstitial? = null
    private var currentScore = 0
    private var highestScore = 0

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

        enableEdgeToEdge()
        setContent {
            Log.d(TAG, "onCreate.setContent")
            ColorBallsTheme {
                val backgroundColor = Color(getColor(R.color.yellow3))
                val topPadding = 30.dp
                Column(modifier = Modifier.fillMaxSize()
                    .background(color = backgroundColor)) {
                    ToolBarMenu(modifier = Modifier.fillMaxWidth().weight(5f)
                        .padding(top = topPadding))

                    Spacer(Modifier.fillMaxWidth().weight(1f))

                    CreateGameView(ROW_COUNT, COLUMN_COUNT,
                        modifier = Modifier.fillMaxWidth().weight(25f))

                    Spacer(Modifier.fillMaxWidth().weight(2f))

                    ShowBannerAds(Modifier.fillMaxWidth().weight(16f))
                }
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        interstitialAd?.releaseInterstitial()
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
            this@MainActivity2, "Undo",
            textFontSize.value,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }
    private fun onClickSettingButton() {
        ScreenUtil.showToast(
            this@MainActivity2, "Setting",
            textFontSize.value,
            ScreenUtil.FontSize_Pixel_Type,
            Toast.LENGTH_SHORT)
    }

    @Composable
    fun ToolBarMenu(modifier: Modifier) {
        Log.d(TAG, "ToolBarMenu")
        Row(modifier = modifier
            .background(color = Color(getColor(R.color.colorPrimary)))) {
            SHowHighestScore(Modifier.weight(1f)
                .align(Alignment.CenterVertically))
            ShowCurrentScore(Modifier.weight(1f)
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
    fun SHowHighestScore(modifier: Modifier) {
        Text(text = highestScore.toString(), modifier = modifier,
            color = Color.Red, fontSize = textFontSize)
    }

    @Composable
    fun ShowCurrentScore(modifier: Modifier) {
        Text(text = currentScore.toString(), modifier = modifier,
            color = Color.White, fontSize = textFontSize)
    }

    @Composable
    fun UndoButton(modifier: Modifier) {
        IconButton (onClick = { onClickUndoButton() }, modifier = modifier
            /*, colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent) */
        ) {
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
    fun CreateGameView(rowCount: Int, columnCount: Int, modifier: Modifier) {
        Log.d(TAG, "CreateGameView")
        Column(modifier = modifier) {
            for (i in 0 until columnCount) {
                Row {
                    for (j in 0 until rowCount) {
                        Image(
                            modifier = Modifier.weight(1f),
                            painter = painterResource(id = R.drawable.box_image),
                            contentDescription = "",
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ShowBannerAds(modifier: Modifier) {
        Log.d(TAG, "ShowBannerAds") // for portrait
        Column(modifier = modifier.background(color = Color.Green),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Text(text = "Show banner ads")
        }
    }
}
