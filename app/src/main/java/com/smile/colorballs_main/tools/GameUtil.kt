package com.smile.colorballs_main.tools

import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichGame

object GameUtil {

    fun getGameId(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.GAME_NO_BARRIER_ID
        WhichGame.HAS_BARRIER -> Constants.GAME_HAS_BARRIER_ID
        WhichGame.REMOVE_BALLS -> Constants.BALLS_REMOVER_ID
        WhichGame.DROP_BALLS -> Constants.DROP_COLOR_BALLS_ID
    }

    fun getDatabaseName(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.NO_BARRIER_DATABASE_NAME
        WhichGame.HAS_BARRIER -> Constants.HAS_BARRIER_DATABASE_NAME
        WhichGame.REMOVE_BALLS -> Constants.BALLS_REMOVER_DATABASE_NAME
        WhichGame.DROP_BALLS -> Constants.DROP_COLOR_BALLS_DATABASE
    }

    fun getSaveFileName(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.SAVE_NO_BARRIER
        WhichGame.HAS_BARRIER -> Constants.SAVE_HAS_BARRIER
        WhichGame.REMOVE_BALLS -> Constants.SAVE_BALLS_REMOVER
        WhichGame.DROP_BALLS -> Constants.SAVE_DROP_BALLS
    }
}