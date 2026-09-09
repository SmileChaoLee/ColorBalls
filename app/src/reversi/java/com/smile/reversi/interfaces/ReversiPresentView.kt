package com.smile.reversi.interfaces

import com.smile.colorballs_main.interfaces.BasePresentView

interface ReversiPresentView : BasePresentView {
    fun getBluePassStr(): String
    fun getRedPassStr(): String
    fun getImageSizeDp(): Float
    fun getWhoWinsMessage(
        redCount: Int,
        blueCount: Int
    ): String
}