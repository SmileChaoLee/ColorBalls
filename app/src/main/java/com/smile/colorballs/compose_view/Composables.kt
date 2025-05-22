package com.smile.colorballs.compose_view

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs.R
import com.smile.colorballs.models.TopPlayer
import com.smile.smilelibraries.utilities.ScreenUtil

object Composables {

    private const val TAG = "Composables"

    interface OkButtonListener {
        fun buttonOkClick(activity: Activity)
    }

    private var textFontSize = 20.sp
    fun setTextFontSize(activity: Activity) {
        /*
        var fSize = ScreenUtil.suitableFontSize(
            activity,
            ScreenUtil.getDefaultTextSizeFromTheme(activity,
                ScreenUtil.FontSize_Pixel_Type, null),
            ScreenUtil.FontSize_Pixel_Type,
            0.0f)
        Log.d(TAG, "setTextFontSize.Pixel.fSize $fSize")
        textFontSize = ScreenUtil.pixelToDp(activity, fSize.toInt()).sp
        Log.d(TAG, "setTextFontSize.sp.textFontSize $textFontSize")
        */

        val fSize = ScreenUtil.suitableFontSize(activity,
            0f, ScreenUtil.FontSize_Pixel_Type,0.0f)
        // Log.d(TAG, "Again.setTextFontSize.Pixel.fSize $fSize")
        textFontSize = ScreenUtil.pixelToDp(activity, fSize.toInt()).sp
        Log.d(TAG, "setTextFontSize.textFontSize.sp = $textFontSize")
    }

    @Composable
    fun Top10Composable(activity: Activity,
                        buttonListener: OkButtonListener,
                        title: String,
                        topPlayers: List<TopPlayer>) {
        Log.d(TAG, "Top10Compose.topPlayers.size = ${topPlayers.size}")
        val columnPaddingTop = 30.dp
        val columnPaddingBottom = 10.dp
        val columnPaddingStart = 0.dp
        val columnPaddingEnd = 0.dp
        // val buttonWidth = 200.dp
        // val buttonHeight = 60.dp
        // val textFontSize = 20.sp
        val textHeight = 25.dp
        val buttonFontSize = 30.sp
        val columnModifier = Modifier.fillMaxSize()
            .padding(top = columnPaddingTop, bottom = columnPaddingBottom,
                start = columnPaddingStart, end = columnPaddingEnd)
            .background(color = Color(0xff90e5c4))

        Column(modifier = columnModifier) {
            LazyColumn(modifier = Modifier.weight(8f, true)) {
                items(topPlayers) { topPlayer->
                    Row {
                        Text(text = topPlayer.player.playerName!!,
                            Modifier
                                // .size(width = 150.dp, height = textHeight)
                                .width(width = 150.dp),
                            color = Color.Red, fontSize = textFontSize)
                        Text(text = topPlayer.player.score!!.toString(),
                            Modifier
                                // .size(width = 100.dp, height = textHeight)
                                .width(width = 100.dp),
                            color = Color.Red, fontSize = textFontSize)
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth()
                .background(color = Color.Blue)
                .weight(1f, true).padding(bottom = columnPaddingBottom),
                horizontalAlignment = Alignment.CenterHorizontally
                , verticalArrangement = Arrangement.Center) {
                Button(onClick = { buttonListener.buttonOkClick(activity) },
                    /* modifier = Modifier
                        .size(buttonWidth, height = buttonHeight)
                        .weight(weight = 1f, fill = true)
                        .align(Alignment.CenterHorizontally)
                        .background(color = Color.Green), */
                    colors = ButtonColors(containerColor = Color.Cyan,
                        disabledContainerColor = Color.DarkGray,
                        contentColor = Color.Red,
                        disabledContentColor = Color.LightGray)
                )
                { Text(text = activity.resources.getString(R.string.okStr),
                    fontFamily = FontFamily.Default, fontSize = textFontSize)
                }
            }
        }
    }
}