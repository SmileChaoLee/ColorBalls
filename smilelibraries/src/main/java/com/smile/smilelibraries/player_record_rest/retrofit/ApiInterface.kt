package com.smile.smilelibraries.player_record_rest.retrofit

import com.smile.smilelibraries.player_record_rest.models.Player
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterface {
    @GET("GetTop10PlayerscoresREST/{gameId}")
    fun getPlayersByGameId(@Path("gameId") gameId: Int): Call<ArrayList<Player>>
}