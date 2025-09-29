package com.smile.colorballs.models

import android.os.Parcelable
import com.smile.colorballs.constants.WhichBall
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorBallInfo(
    var ballColor: Int = 0,
    var whichBall: WhichBall = WhichBall.NO_BALL,
    var isAnimation: Boolean = false,
    var isResize: Boolean = false)
    :Parcelable