package com.smile.smilelibraries.retrofit

import android.util.Log
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
object Client {
    private const val TAG = "Client"
    private const val BASE_URL = "http://137.184.120.171/"

    // For emulator
    // private static final String BASE_URL = "http://10.0.2.2:5000/";
    // For Physical Android Phone
    // private static final String BASE_URL = "http://192.168.0.108:5000/";
    private var retrofit: Retrofit? = null

    @JvmStatic
    @get:Provides
    val instance: Retrofit
        get() {
            Log.d(TAG, "getInstance")
            if (retrofit == null) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS).build()
                val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit!!
        }
}
