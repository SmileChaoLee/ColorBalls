package com.smile.colorballs_main.views

import android.content.res.Configuration
import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import com.smile.colorballs_main.tools.LogUtil

abstract class CbRmBaseView: BaseView() {

    companion object {
        private const val TAG = "CbRmBaseView"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtil.i(TAG, "$TAG.onCreate")
        // Must be before super.onCreate(savedInstanceState)
        gameWidthRation = 1.0f
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            menuBarWeight = 1.0f
            gameGridWeight = 7.0f
        } else {
            menuBarWeight = 1.0f
            gameGridWeight = 9.0f
        }
        LogUtil.i(TAG, "$TAG.onCreate.menuBarWeight = $menuBarWeight")
        LogUtil.i(TAG, "$TAG.onCreate.gameGridWeight = $gameGridWeight")

        super.onCreate(savedInstanceState)
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