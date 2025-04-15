package com.smile.colorballs.constants

class Constants {
    companion object {
        const val GAME_ID = "1"
        const val HAS_SOUND = "HasSound"
        const val IS_EASY_LEVEL = "IsEasyLevel"
        const val HAS_NEXT_BALL = "HasNextBall"
        const val TOP10_TITLE_NAME = "Top10TitleName"
        const val TOP10_PLAYERS = "Top10Players"
        const val TOP10_SCORES = "Top10Scores"
        const val PLAYER_NAMES = "PlayerNames"
        const val PLAYER_SCORES = "PlayerScores"
        const val TOP10_RESULT = "Top10Result"
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
        @JvmField
        val BallColor =
            intArrayOf(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_MAGENTA, COLOR_YELLOW, COLOR_CYAN)
        const val BALL_NUM_ONE_TIME : Int = 3
    }
}