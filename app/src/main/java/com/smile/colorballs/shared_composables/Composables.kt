package com.smile.colorballs.shared_composables

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.colorballs.R
import com.smile.colorballs.models.TopPlayer
import com.smile.smilelibraries.utilities.ScreenUtil

object Composables {

    private const val TAG = "Composables"

    interface OkButtonListener {
        fun buttonOkClick()
        fun buttonCancelClick() = run { }
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
    fun HorDivider(color: Color = Color.Black, thickness: Dp = 2.dp,
                   modifier: Modifier = Modifier) {
        HorizontalDivider(modifier = modifier.fillMaxWidth(),
            color = color, thickness = thickness)
    }

    @Composable
    fun MenuItemText(text: String, color: Color,
                     modifier: Modifier = Modifier) {
        Text(text = text, color = color, modifier = modifier,
            fontWeight = FontWeight.Light, fontStyle = FontStyle.Normal,
            fontSize = menuItemFontSize)
    }

    @Composable
    fun DropdownMenuItem(text: String, color: Color,
                         onClick: () -> Unit,
                         isDivider: Boolean = true) {
        val itemHeight = textUnitToDp((menuItemFontSize.value+3).sp)
        androidx.compose.material3.DropdownMenuItem(
            modifier = Modifier
                .height(height = itemHeight)
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
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xff90e5c4))) {
            Text(text = title, fontSize = mFontSize, color = Color.Blue)
            HorizontalDivider(color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(10.dp))
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
                            modifier = Modifier
                                .weight(2f)
                                .size(imageWidth),
                            painter = painterResource(id = topPlayer.medal),
                            contentDescription = "", // Accessibility text
                            contentScale = ContentScale.Fit
                        )
                    }
                    HorizontalDivider(color = Color.Blue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(5.dp))
                }
            }
            Column(modifier = Modifier
                .fillMaxWidth()
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

    @Composable
    fun SettingCompose(activity: Activity, text: String,
                       hasSound: Boolean, isEasy: Boolean,
                       hasNextBall: Boolean, backgroundColor: Color) {
        /*
        val windowSize = LocalWindowInfo.current.containerSize
        val screenWidth = windowSize.width
        val screenHeight = windowSize.height    // contains navigation bar
        */
        val textColor = Color(0xffffff00)
        val spaceWeight = 1.0f
        val dividerWeight = 1.0f
        var setRowWeight = 3.0f    // for setting row
        var setColumnWeight = 8.0f     // for setting column
        var rowWeight = 5.0f
        var textWeight = 3.0f
        var buttonWeight = spaceWeight * 3.0f
        if (LocalConfiguration.current.orientation ==
            Configuration.ORIENTATION_LANDSCAPE) {
            setRowWeight = 8.0f
            setColumnWeight = 6.0f
            // columnWidth = screen.x.toFloat() * 0.6f
            rowWeight = 8.0f
            textWeight = 6.0f
            buttonWeight = spaceWeight * 2.0f
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)) { // main column
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .weight(spaceWeight))
            Row(modifier = Modifier.weight(setRowWeight)) {    // setting row
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .weight(spaceWeight))
                Column(
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .weight(setColumnWeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { // setting column
                    Column(modifier = Modifier.weight(textWeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        MenuItemText(text = text, Color.White)
                    }
                    HorDivider(
                        color = Color.White,
                        modifier = Modifier.weight(dividerWeight)
                    )
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MenuItemText(
                            text = activity.getString(R.string.soundStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            text = activity.getString(R.string.onStr),
                            Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            text = activity.getString(R.string.yesStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MenuItemText(
                            text = activity.getString(R.string.playerLevelStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            text = activity.getString(R.string.no1),
                            Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            text = activity.getString(R.string.easyStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MenuItemText(
                            text = activity.getString(R.string.nextBallSettingStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            text = activity.getString(R.string.onStr),
                            Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            text = activity.getString(R.string.showStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(buttonWeight),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                disabledContentColor = Color.Yellow,
                                contentColor = Color.Red,
                                disabledContainerColor = Color.Red,
                            )
                        ) {
                            Text(
                                text = activity.getString(R.string.cancelStr),
                                fontSize = mFontSize
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(spaceWeight)
                        )
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(buttonWeight),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                disabledContentColor = Color.Yellow,
                                contentColor = Color.Blue,
                                disabledContainerColor = Color.Blue
                            )
                        ) {
                            Text(
                                text = activity.getString(R.string.okStr),
                                fontSize = mFontSize
                            )
                        }
                    }
                }   // end of setting column
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .weight(spaceWeight))
            }   // end of setting row
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .weight(spaceWeight))
        }   // end of main column
    }
}