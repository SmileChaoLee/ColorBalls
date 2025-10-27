package com.smile.colorballs_main.roomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smile.colorballs_main.tools.LogUtil
import com.smile.smilelibraries.models.Player

@Database(entities = [Score::class], version = 2)
abstract class ScoreDatabase : RoomDatabase() {

    abstract fun scoreDao(): ScoreDao

    companion object {
        private const val TAG = "ScoreDatabase"
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Example: Add a new column to the 'users' table
                db.execSQL("DROP TABLE IF EXISTS score")
            }
        }
        fun getDatabase(context: Context, databaseName: String): ScoreDatabase {
            LogUtil.i(TAG, "getDatabase")
            return synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScoreDatabase::class.java,
                    databaseName
                )
                    // IMPORTANT: Add migration strategies here if your schema changes
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Example
                    .addMigrations(MIGRATION_1_2)
                    .build()
                instance
            }
        }
    }

    suspend fun addScore(score: Score): Long {
        LogUtil.i(TAG, "addScore.score = $score")
        val tempScore = Score(  // use the default value, null for id
            playerName = score.playerName,
            playerScore = score.playerScore
        )
        try {
            return scoreDao().insertOneScore(tempScore)
        } catch (ex: Exception) {
            LogUtil.e(TAG, "addScore.Exception: ", ex)
            return -1L  // Return -1 to indicate an error
        }
    }

    suspend fun getHighestScore(): Int {
        try {
            return scoreDao().getHighestScore()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getHighestScore.Exception: ", ex)
            return -1  // Return -1 to indicate an error
        }
    }

    suspend fun isInTop10(score: Int): Boolean {
        LogUtil.i(TAG, "isInTop10.score = $score")
        try {
            val top10Scores = scoreDao().readTop10ScoreList()
            return if (top10Scores.size < 10) {
                true
            } else {
                score >= (top10Scores.last().playerScore ?: 0)
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, "isInTop10.Exception: ", ex)
            return false  // Return false to indicate an error
        }
    }

    suspend fun getLocalTop10(): ArrayList<Player> {
        LogUtil.i(TAG, "getLocalTop10")
        val players = ArrayList<Player>()
        try {
            val top10Scores = scoreDao().readTop10ScoreList()
            for (score in top10Scores) {
                val player = Player(
                    id = score.id?.toInt(),
                    playerName = score.playerName,
                    score = score.playerScore,
                    gameId = -1 // not be used in this context
                )
                players.add(player)
            }
        } catch (ex: Exception) {
            LogUtil.e(TAG, "getLocalTop10.Exception: ", ex)
        }
        return players
    }

    suspend fun deleteAllAfterTop10() {
        LogUtil.i(TAG, "deleteAllAfterTop10")
        try {
            scoreDao().deleteAllAfterTop10()
        } catch (ex: Exception) {
            LogUtil.e(TAG, "deleteAllAfterTop10.Exception: ", ex)
        }
    }
}
