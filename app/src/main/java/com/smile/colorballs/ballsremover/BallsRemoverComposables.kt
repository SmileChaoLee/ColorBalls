package com.smile.colorballs.ballsremover

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smile.colorballs.ballsremover.constants.BallsRemoverConstants
import com.smile.colorballs.ballsremover.models.Settings
import com.smile.colorballs.R
import com.smile.colorballs.views.CbComposable

object BallsRemoverComposables {

    @Composable
    fun SettingCompose(
        activity: Activity,
        buttonListener: CbComposable.ButtonClickListener,
        textListener: CbComposable.BRemSettingClickListener,
        text: String, backgroundColor: Color,
        setting: Settings
    ) {
        val textColor = Color(0xffffa500)
        val spaceWeight = 1.0f
        val dividerWeight = 1.0f
        var setRowWeight = 3.0f    // for setting row
        var setColumnWeight = 10.0f     // for setting column
        var rowWeight = 5.0f
        var textWeight = 3.0f
        var buttonWeight = spaceWeight * 3.0f
        if (LocalConfiguration.current.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        ) {
            setRowWeight = 8.0f
            setColumnWeight = 6.0f
            rowWeight = 8.0f
            textWeight = 6.0f
            buttonWeight = spaceWeight * 2.0f
        }

        val onStr = activity.getString(R.string.onStr)
        val offStr = activity.getString(R.string.offStr)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
        ) { // main column
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(spaceWeight)
            )
            val yesStr = activity.getString(R.string.yesStr)
            val noStr = activity.getString(R.string.noStr)
            Row(modifier = Modifier.weight(setRowWeight)) {    // setting row
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(spaceWeight)
                )
                Column(
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .weight(setColumnWeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { // setting column
                    Column(
                        modifier = Modifier.weight(textWeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = text, color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = CbComposable.mFontSize
                        )
                    }
                    CbComposable.HorDivider(
                        color = Color.White,
                        modifier = Modifier.weight(dividerWeight)
                    )
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        var hasSound by remember { mutableStateOf(setting.hasSound) }
                        CbComposable.MenuItemText(
                            text = activity.getString(R.string.soundStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        CbComposable.MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                                .clickable {
                                    hasSound = !hasSound
                                    textListener.hasSoundClick(hasSound)
                                },
                            text = if (hasSound) onStr else offStr,
                            Color.White
                        )
                        CbComposable.MenuItemText(
                            text = if (hasSound) yesStr else noStr,
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
                        val no1Str = activity.getString(R.string.no1)
                        val no2Str = activity.getString(R.string.no2)
                        val easyStr = activity.getString(R.string.easyStr)
                        val diffStr = activity.getString(R.string.difficultStr)
                        var gameLevel by remember { mutableIntStateOf(setting.gameLevel) }
                        CbComposable.MenuItemText(
                            text = activity.getString(R.string.playerLevelStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        CbComposable.MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                                .clickable {
                                    gameLevel = if (gameLevel == BallsRemoverConstants.EASY_LEVEL) BallsRemoverConstants.DIFFICULT_LEVEL
                                    else BallsRemoverConstants.EASY_LEVEL
                                    textListener.gameLevelClick(gameLevel)
                                },
                            text = if (gameLevel == BallsRemoverConstants.EASY_LEVEL) no1Str else no2Str,
                            Color.White
                        )
                        CbComposable.MenuItemText(
                            text = if (gameLevel == BallsRemoverConstants.EASY_LEVEL) easyStr else diffStr,
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
                        var fillColumn by remember { mutableStateOf(setting.fillColumn) }
                        CbComposable.MenuItemText(
                            text = activity.getString(R.string.fillColumnStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        CbComposable.MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp).clickable {
                                    fillColumn = !fillColumn
                                    textListener.isFillColumnClick(fillColumn)
                                },
                            text = if (fillColumn) onStr else offStr,
                            Color.White
                        )
                        CbComposable.MenuItemText(
                            text = if (fillColumn) yesStr else noStr,
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
                            onClick = { buttonListener.buttonCancelClick() },
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
                                fontSize = CbComposable.mFontSize
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(spaceWeight)
                        )
                        Button(
                            onClick = { buttonListener.buttonOkClick() },
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
                                fontSize = CbComposable.mFontSize
                            )
                        }
                    }
                }   // end of setting column
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(spaceWeight)
                )
            }   // end of setting row
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(spaceWeight)
            )
        }   // end of main column
    }
}