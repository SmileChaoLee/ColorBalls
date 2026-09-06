package com.smile.colorballs_main.tools

import com.smile.colorballs_main.constants.Constants
import com.smile.colorballs_main.constants.WhichGame
import java.io.FileOutputStream

object GameUtil {

    fun getGameId(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.GAME_NO_BARRIER_ID
        WhichGame.HAS_BARRIER -> Constants.GAME_HAS_BARRIER_ID
        WhichGame.REMOVE_BALLS -> Constants.BALLS_REMOVER_ID
        WhichGame.DROP_BALLS -> Constants.DROP_COLOR_BALLS_ID
        WhichGame.REVERSI -> Constants.REVERSI_ID
    }

    fun getDatabaseName(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.NO_BARRIER_DATABASE_NAME
        WhichGame.HAS_BARRIER -> Constants.HAS_BARRIER_DATABASE_NAME
        WhichGame.REMOVE_BALLS -> Constants.BALLS_REMOVER_DATABASE_NAME
        WhichGame.DROP_BALLS -> Constants.DROP_COLOR_BALLS_DATABASE
        WhichGame.REVERSI -> Constants.REVERSI_DATABASE
    }

    fun getSaveFileName(whichGame: WhichGame) = when(whichGame) {
        WhichGame.NO_BARRIER -> Constants.SAVE_NO_BARRIER
        WhichGame.HAS_BARRIER -> Constants.SAVE_HAS_BARRIER
        WhichGame.REMOVE_BALLS -> Constants.SAVE_BALLS_REMOVER
        WhichGame.DROP_BALLS -> Constants.SAVE_DROP_BALLS
        WhichGame.REVERSI -> Constants.SAVE_REVERSI
    }

    fun saveGameLevel(foStream: FileOutputStream, level: Int) {
        when (level) {
            Constants.GAME_LEVEL_1 -> foStream.write(1)
            Constants.GAME_LEVEL_2 -> foStream.write(2)
            Constants.GAME_LEVEL_3 -> foStream.write(3)
            Constants.GAME_LEVEL_4 -> foStream.write(4)
            Constants.GAME_LEVEL_5 -> foStream.write(5)
            else -> foStream.write(2)
        }
    }

    fun translateGameLevel(level: Int): Int {
        return when (level) {
            1 ->Constants.GAME_LEVEL_1
            2 ->Constants.GAME_LEVEL_2
            3 ->Constants.GAME_LEVEL_3
            4 ->Constants.GAME_LEVEL_4
            5 ->Constants.GAME_LEVEL_5
            else -> Constants.GAME_LEVEL_2
        }
    }
}