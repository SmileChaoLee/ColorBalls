package com.smile.colorballs_main.constants

object Constants {
    const val GAME_NO_BARRIER_ID = "1"
    const val BALLS_REMOVER_ID = "5"
    const val GAME_HAS_BARRIER_ID = "6"
    const val DROP_COLOR_BALLS_ID = "7"
    const val FIVE_COLOR_BALLS_ID = "8"
    const val GAME_ID = "GameId"
    const val NO_BARRIER_DATABASE_NAME = "colorBallDatabase.db"
    const val HAS_BARRIER_DATABASE_NAME = "colorBallDatabase1.db"
    const val BALLS_REMOVER_DATABASE_NAME = "balls_remover.db"
    const val DROP_COLOR_BALLS_DATABASE = "dropColorBallDatabase.db"
    const val DATABASE_NAME = "DatabaseName"
    const val SAVE_NO_BARRIER = "SavedGame"
    const val SAVE_HAS_BARRIER = "SavedGame1"
    const val SAVE_BALLS_REMOVER = "SaveBallsRemover"
    const val SAVE_DROP_BALLS = "SavedDropColorBalls"
    const val HAS_SOUND = "HasSound"
    const val HAS_NEXT = "HasNextBall"
    const val TOP10_PLAYERS = "Top10Players"
    const val IS_LOCAL_TOP10 = "IsLocalTop10"
    const val GAME_ID_STRING = "GameIdString"
    // 10->RED, 20->GREEN, 30->BLUE, 40->MAGENTA, 50->YELLOW, 60->Cyan
    const val COLOR_RED = 10
    const val COLOR_GREEN = 20
    const val COLOR_BLUE = 30
    const val COLOR_YELLOW = 40
    const val COLOR_CYAN = 50
    const val COLOR_MAGENTA = 60
    const val COLOR_BARRIER = 100
    const val COLOR_FIREWORK = 101
    const val NUM_BALLS_USED_EASY= 5
    // 6 colors for difficult level
    const val NUM_BALLS_USED_DIFF = 6
    @JvmField
    val BallColor =
        intArrayOf(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_YELLOW, COLOR_CYAN, COLOR_MAGENTA)
    const val GAME_PROP_TAG = "GameProp"
    const val GRID_DATA_TAG = "GridData"

    const val IS_CREATING_GAME = 1
    const val IS_QUITING_GAME = 0

    const val GLOBAL_TOP10_ACTION_NAME = "com.smile.Service.GlobalTop10"
    const val LOCAL_TOP10_ACTION_NAME = "com.smile.Service.LocalTop10"

    const val GAME_LEVEL = "GameLevel"
    const val GAME_LEVEL_1 = 1
    const val GAME_LEVEL_2 = 2
    const val GAME_LEVEL_3 = 3
    const val GAME_LEVEL_4 = 4
    const val GAME_LEVEL_5 = 5
}