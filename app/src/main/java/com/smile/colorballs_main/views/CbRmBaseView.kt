package com.smile.colorballs_main.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smile.colorballs_main.tools.LogUtil

abstract class CbRmBaseView: BaseView() {

    companion object {
        private const val TAG = "CbRmBaseView"
    }

    // implement abstract fun of BaseView
    @Composable
    override fun ToolBarMenu(modifier: Modifier) {
        LogUtil.i(TAG, "ToolBarMenu.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Row(modifier = modifier
            // .background(color = Color(getColor(R.color.colorPrimary)))) {
            // .background(android.graphics.Color.rgb(0x3F, 0x51, 0xB5))) {
            .background(colorPrimary)) {
            ShowCurrentScore(modifier = Modifier.weight(2f)
                    .padding(start = 10.dp)
                    .align(Alignment.CenterVertically),
                pFontSize = CbComposable.mFontSize)
            SHowHighestScore(modifier = Modifier.weight(2f)
                    .align(Alignment.CenterVertically),
                pFontSize = CbComposable.mFontSize)
            UndoButton(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
            SettingButton(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
            ShowMenu(modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically))
        }
    }

    @Composable
    override fun GameViewGrid() {
        LogUtil.i(TAG, "GameViewGrid.mOrientation.intValue" +
                " = ${mOrientation.intValue}")
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Box(contentAlignment = Alignment.Center) {
                ShowGameGrid()
                ShowMessageOnScreen()
            }
        }
    }
    // end of implementing abstract fun of BaseView
}