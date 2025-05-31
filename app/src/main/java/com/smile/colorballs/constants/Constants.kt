package com.smile.colorballs.constants

import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R

class Constants {
    companion object {
        const val GAME_ID = "1"
        const val HAS_SOUND = "HasSound"
        const val IS_EASY_LEVEL = "IsEasyLevel"
        const val HAS_NEXT_BALL = "HasNextBall"
        const val TOP10_TITLE_NAME = "Top10TitleName"
        const val TOP10_PLAYERS = "Top10Players"
        const val IS_LOCAL_TOP10 = "IsLocalTop10"
        const val GAME_ID_STRING = "GameIdString"
        const val SURE_SAVE_DIALOG_TAG = "SureSaveDialogTag"
        const val SURE_LOAD_DIALOG_TAG = "SureLoadDialogTag"
        const val GAME_OVER_DIALOG_TAG = "GameOverDialogTag"
        // 10->RED, 20->GREEN, 30->BLUE, 40->MAGENTA, 50->YELLOW, 60->Cyan
        const val COLOR_RED = 10
        const val COLOR_GREEN = 20
        const val COLOR_BLUE = 30
        const val COLOR_MAGENTA = 40
        const val COLOR_YELLOW = 50
        const val COLOR_CYAN = 60
        const val BALL_NUM_COMPLETED = 5
        const val NUM_EASY= 5
        // 6 colors for difficult level
        const val NUM_DIFFICULT = 6
        @JvmField
        val ROW_COUNTS = ColorBallsApp.mResources.getInteger(R.integer.rowCounts)
        @JvmField
        val COLUMN_COUNTS = ColorBallsApp.mResources.getInteger(R.integer.columnCounts)
        @JvmField
        val BallColor =
            intArrayOf(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_MAGENTA, COLOR_YELLOW, COLOR_CYAN)
        const val BALL_NUM_ONE_TIME : Int = 3
        const val GAME_PROP_TAG = "GameProp"
        const val GRID_DATA_TAG = "GridData"

        const val GLOBAL_TOP10_ACTION_NAME = "com.smile.Service.GlobalTop10"
        const val LOCAL_TOP10_ACTION_NAME = "com.smile.Service.LocalTop10"
    }
}