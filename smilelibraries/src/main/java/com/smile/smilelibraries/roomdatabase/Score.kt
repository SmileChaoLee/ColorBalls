package com.smile.smilelibraries.roomdatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score")
data class Score(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = 0,

    @ColumnInfo(name = "playerName")
    var playerName: String = "",

    @ColumnInfo(name = "playerScore")
    var playerScore: Int? = 0
)