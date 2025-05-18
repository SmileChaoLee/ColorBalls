package com.smile.smilelibraries.retrofit

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Client {

    private const val TAG = "Client"

    // private const val BASE_URL = "http://137.184.120.171/"
    private val retrofitMap: HashMap<String, Retrofit> = HashMap()

    fun getInstance(webUrl: String): Retrofit {
        val retrofit: Retrofit
        if (retrofitMap.contains(webUrl)) {
            retrofit = retrofitMap.getValue(webUrl)
        } else {
            retrofit = getRetrofit(webUrl)
            retrofitMap[webUrl] = retrofit
        }
        Log.d(TAG, "retrofit = $retrofit")
        return retrofit
    }

    private fun getRetrofit(webUrl: String): Retrofit {
        Log.d(TAG, "getInstance.url = $webUrl")
        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS).build()
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
            .baseUrl(webUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        Log.d(TAG, "getRetrofit.retrofit = $retrofit")
        return retrofit
    }
}
