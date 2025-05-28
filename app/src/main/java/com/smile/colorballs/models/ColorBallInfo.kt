package com.smile.colorballs.models

import android.os.Parcelable
import com.smile.colorballs.constants.WhichBall
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorBallInfo(var ballColor: Int = 0,
                         var resizeRatio: Float = 1.0f,
                         var whichBall: WhichBall = WhichBall.BALL,
                         var isAnimation: Boolean = false)
    :Parcelable