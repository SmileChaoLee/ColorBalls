package com.smile.smilelibraries.player_record_rest.models

import android.os.Parcelable
import androidx.databinding.BaseObservable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
open class Player(
    @SerializedName("Id")
    var id: Int? = 0,
    @SerializedName("PlayerName")
    var playerName: String? = "",
    @SerializedName("Score")
    var score: Int? = 0,
    @SerializedName("GameId")
    var gameId: Int? = 0): Parcelable