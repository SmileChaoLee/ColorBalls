package com.smile.colorballs.tools

import com.smile.colorballs.constants.Constants
import com.smile.colorballs.constants.WhichGame

object Utils {

    fun getGameId(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.GAME_NO_BARRIER_ID
        WhichGame.HAS_BARRIER -> Constants.GAME_HAS_BARRIER_ID
        WhichGame.RESOLVE_GRID -> Constants.GAME_RESOLVE_GRID_ID
        WhichGame.REMOVE_BALLS -> Constants.BALLS_REMOVER_GAME_ID
    }

    fun getDatabaseName(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.NO_BARRIER_DATABASE_NAME
        WhichGame.HAS_BARRIER -> Constants.HAS_BARRIER_DATABASE_NAME
        WhichGame.RESOLVE_GRID -> Constants.RESOLVE_GRID_DATABASE_NAME
        WhichGame.REMOVE_BALLS -> Constants.BALLS_REMOVER_DATABASE_NAME
    }

    fun getSaveFileName(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.SAVE_NO_BARRIER
        WhichGame.HAS_BARRIER -> Constants.SAVE_HAS_BARRIER
        WhichGame.RESOLVE_GRID -> Constants.SAVE_RESOLVE_GRID
        WhichGame.REMOVE_BALLS -> Constants.SAVE_BALLS_REMOVER
    }
}