package com.smile.smilelibraries.player_record_rest.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    @SerializedName("Id")
    var id: Int? = 0,
    @SerializedName("PlayerName")
    var playerName: String? = "",
    @SerializedName("Score")
    var score: String? = "",
    @SerializedName("GameId")
    var gameId: Int? = 0): Parcelable