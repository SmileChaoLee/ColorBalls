package com.smile.smilelibraries.player_record_rest.retrofit

import android.util.Log
import com.smile.smilelibraries.models.Player
import com.smile.smilelibraries.retrofit.Client
import retrofit2.Response

object RestApiSync {

    private const val TAG = "RestApiSync"
    // get Retrofit client and Retrofit Api
    private val apiInstanceMap: HashMap<String, ApiInterface> = HashMap()
    private fun getApiInstance(webUrl: String): ApiInterface {
        val apiInstance: ApiInterface
        if (apiInstanceMap.contains(webUrl)) {
            apiInstance = apiInstanceMap.getValue(webUrl)
        } else {
            apiInstance = Client.getInstance(webUrl).create(ApiInterface::class.java)
            apiInstanceMap[webUrl] = apiInstance
        }
        Log.d(TAG, "getApiInstance = $apiInstance")
        return apiInstance
    }

    fun getPlayersByGameId(webUrl: String, gameId: Int):
            ArrayList<Player> {
        Log.d(TAG, "getPlayersByGameId")
        // get Call from Retrofit Api
        try {
            val response: Response<ArrayList<Player>> = getApiInstance(webUrl)
                .getPlayersByGameId(gameId).execute()
            return response.body() ?: ArrayList()
        } catch (ex: Exception) {
            Log.d(TAG, "getPlayersByGameId().Exception")
            ex.printStackTrace()
            return ArrayList()
        }
    }
}