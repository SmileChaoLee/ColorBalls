package com.smile.colorballs.model

import com.google.gson.annotations.SerializedName

class PlayerList {
    @SerializedName("pageNo")
    var pageNo = 0

    @SerializedName("pageSize")
    var pageSize = 0

    @SerializedName("totalRecords")
    var totalRecords = 0

    @SerializedName("totalPages")
    var totalPages = 0

    @SerializedName("players")
    var players: ArrayList<Player> = ArrayList()
}
