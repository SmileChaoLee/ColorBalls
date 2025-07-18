package com.smile.smilelibraries.roomdatabase

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        fun getDatabase(context: Context): ScoreDatabase {
            Log.d(TAG, "getDatabase")
            return synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScoreDatabase::class.java,
                    "colorBallDatabase.db"
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
        Log.d(TAG, "addScore.score = $score")
        return scoreDao().insertOneScore(score)
    }

    suspend fun getHighestScore(): Int {
        return scoreDao().getHighestScore()
    }

    suspend fun isInTop10(score: Int): Boolean {
        Log.d(TAG, "isInTop10.score = $score")
        val top10Scores = scoreDao().readTop10ScoreList()
        return if (top10Scores.size < 10) {
            true
        } else {
            score >= (top10Scores.last().playerScore ?: 0)
        }
    }

    suspend fun getLocalTop10(): ArrayList<Player> {
        Log.d(TAG, "getLocalTop10")
        val top10Scores = scoreDao().readTop10ScoreList()
        val players = ArrayList<Player>()
        for (score in top10Scores) {
            val player = Player(
                id = score.id?.toInt(),
                playerName = score.playerName,
                score = score.playerScore,
                gameId = -1 // not be used in this context
            )
            players.add(player)
        }
        return players
    }
}
