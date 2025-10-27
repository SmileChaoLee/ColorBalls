package com.smile.colorballs_main.roomdatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertOneScore(score: Score): Long

    @Query("SELECT * FROM score")
    fun getAllScores(): Flow<List<Score>>?

    @Query("SELECT * FROM score WHERE id = :scoreId")
    suspend fun getScoreById(scoreId: Long): Score?

    @Query("SELECT playerScore FROM score ORDER BY playerScore DESC LIMIT 1")
    suspend fun getHighestScore(): Int

    @Query("SELECT * FROM score ORDER BY playerScore DESC LIMIT 10")
    suspend fun readTop10ScoreList(): List<Score>

    // implemented in ScoreDatabase
    // suspend fun isInTop10(score: Int): Boolean

    @Query("DELETE FROM score WHERE id = :scoreId")
    suspend fun deleteOneScoreById(scoreId: Long): Int

    @Delete
    suspend fun deleteOneScore(score: Score)

    @Query("DELETE FROM score WHERE id NOT IN (SELECT id FROM score ORDER BY playerScore DESC LIMIT 10)")
    suspend fun deleteAllAfterTop10(): Int

    @Query("DELETE FROM score")
    suspend fun deleteAllScore()
}