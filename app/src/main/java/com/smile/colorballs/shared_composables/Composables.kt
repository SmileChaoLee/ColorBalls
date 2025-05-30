package com.smile.colorballs.shared_composables

import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs.models.TopPlayer

object Composables {

    private const val TAG = "Composables"

    interface OkButtonListener {
        fun buttonOkClick()
    }

    private val mResources = Resources.getSystem()

    var mFontSize = 24.sp
        set(value) {
            if (field != value) {
                field = value
                menuItemFontSize = value
            }
        }

    private var menuItemFontSize = mFontSize

    @Composable
    fun textUnitToDp(sp: TextUnit): Dp {
        val dp = with(LocalDensity.current) {
            sp.toDp()
        }
        return dp
    }

    @Composable
    fun HorDivider(color: Color = Color.Black, thickness: Dp = 2.dp) {
        HorizontalDivider(color = color,
            modifier = Modifier.fillMaxWidth().padding(all = 0.dp),
            thickness = thickness)
    }

    @Composable
    fun MenuItemText(text: String, color: Color) {
        Text(text = text, color = color,
            fontWeight = FontWeight.Light, fontStyle = FontStyle.Normal,
            fontSize = menuItemFontSize)
    }

    @Composable
    fun DropdownMenuItem(text: String, color: Color,
                         onClick: () -> Unit,
                         isDivider: Boolean = true) {
        val itemHeight = textUnitToDp((menuItemFontSize.value+3).sp)
        androidx.compose.material3.DropdownMenuItem(
            modifier = Modifier.height(height = itemHeight)
                .padding(all = 0.dp),
            text = { MenuItemText(text = text, color = color ) },
            onClick = { onClick() })
        if (isDivider) HorDivider()
    }

    // For the Top10ComposeActivity
    @Composable
    fun Top10Composable(title: String, topPlayers: List<TopPlayer>,
                        buttonListener: OkButtonListener, oKStr: String) {
        Log.d(TAG, "Top10Compose.topPlayers.size = ${topPlayers.size}")
        val imageWidth = (mFontSize.value * 3.0).dp
        Column(modifier = Modifier.fillMaxSize()
            .background(color = Color(0xff90e5c4))) {
            Text(text = title, fontSize = mFontSize, color = Color.Blue)
            HorizontalDivider(color = Color.Black,
                modifier = Modifier.fillMaxWidth().size(10.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(topPlayers) { topPlayer->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(3f)) {
                            Text(text = topPlayer.player.playerName!!,
                                color = Color.Red, fontSize = mFontSize)
                            Text(text = topPlayer.player.score!!.toString(),
                                color = Color.Red, fontSize = mFontSize)
                        }
                        Image(
                            modifier = Modifier.weight(2f)
                            .size(imageWidth),
                            painter = painterResource(id = topPlayer.medal),
                            contentDescription = "", // Accessibility text
                            contentScale = ContentScale.Fit
                        )
                    }
                    HorizontalDivider(color = Color.Blue,
                        modifier = Modifier.fillMaxWidth().size(5.dp))
                }
            }
            Column(modifier = Modifier.fillMaxWidth()
                .background(color = Color.Blue)/*.weight(10f, true)*/,
                horizontalAlignment = Alignment.CenterHorizontally
                , verticalArrangement = Arrangement.Center) {
                Button(onClick = { buttonListener.buttonOkClick() },
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
                { Text(text = oKStr, fontSize = mFontSize) }
            }
        }
    }
}