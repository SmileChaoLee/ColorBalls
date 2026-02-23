package com.smile.fivecolorballs.constants

import com.smile.colorballs_main.constants.Constants.COLOR_BLUE
import com.smile.colorballs_main.constants.Constants.COLOR_CYAN
import com.smile.colorballs_main.constants.Constants.COLOR_GREEN
import com.smile.colorballs_main.constants.Constants.COLOR_MAGENTA
import com.smile.colorballs_main.constants.Constants.COLOR_RED
import com.smile.colorballs_main.constants.Constants.COLOR_YELLOW

object FiveBallsConstants {
    const val FIVE_COLOR_BALLS_ID = "8"
    const val FIVE_COLOR_BALLS_DATABASE = "colorBallDatabase.db"
    const val BALL_NUM_ONE_TIME : Int = 3
    @JvmField
    val BallColor =
        intArrayOf(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_YELLOW, COLOR_MAGENTA, COLOR_CYAN)
}