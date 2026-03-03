package com.example.foundbuddy.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Emulator: http://10.0.2.2:8080/
    // Handy im WLAN: http://DEIN-PC-IP:8080/
    private const val BASE_URL = "https://foundbuddy-rzyh.onrender.com/"

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)   // CLIP braucht Zeit beim HF Space Cold Start
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val moshi by lazy {
        com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
