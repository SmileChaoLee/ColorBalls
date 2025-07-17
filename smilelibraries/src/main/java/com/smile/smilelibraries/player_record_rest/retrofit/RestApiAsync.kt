package com.smile.smilelibraries.player_record_rest.retrofit

import android.util.Log
import com.smile.smilelibraries.models.Player
import com.smile.smilelibraries.retrofit.Client
import retrofit2.Callback

abstract class RestApiAsync<T> : Callback<T> {
    companion object {
        private const val TAG = "RestApiAsync"
    }

    abstract fun getWebUrl(): String

    private val callback : Callback<T>
        get() {
            return this
        }

    // get Retrofit client and Retrofit Api
    private val apiInterface : ApiInterface
        get() {
            return Client.getInstance(getWebUrl()).create(ApiInterface::class.java)
        }

    @Suppress("UNCHECKED_CAST")
    fun getPlayersByGameId(gameId: Int) {
        Log.d(TAG, "getComments")
        // get Call from Retrofit Api
        apiInterface.getPlayersByGameId(gameId)
            .enqueue(callback as Callback<ArrayList<Player>>)
    }
}