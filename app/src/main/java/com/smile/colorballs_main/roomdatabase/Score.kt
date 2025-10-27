package com.smile.colorballs_main.roomdatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score")
data class Score(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    var id: Long? = null,

    @ColumnInfo(name = "playerName")
    var playerName: String = "",

    @ColumnInfo(name = "playerScore")
    var playerScore: Int? = 0
)