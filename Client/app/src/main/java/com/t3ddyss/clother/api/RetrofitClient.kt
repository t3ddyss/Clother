package com.t3ddyss.clother.api

import android.os.Build
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:5000/"
    private const val BASE_URL_DEVICE = "http://192.168.0.104:5000/"

    // Checking if app's running from the emulator for debug purposes
    val instance: ClotherAuthService by lazy {
        if (Build.FINGERPRINT.contains("generic"))
            Retrofit.Builder()
                    .baseUrl(BASE_URL_EMULATOR)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                    .build()
                    .create(ClotherAuthService::class.java)
        else Retrofit.Builder()
                .baseUrl(BASE_URL_DEVICE)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
                .create(ClotherAuthService::class.java)
    }
}