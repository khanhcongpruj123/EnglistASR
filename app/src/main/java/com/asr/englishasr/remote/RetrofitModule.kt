package com.asr.englishasr.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitModule {

    private var instance : Retrofit? = null

    fun getInstance() : Retrofit? {

        if (instance == null) {

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build()

            instance = Retrofit.Builder()
                .baseUrl("http://103.74.122.136:8077/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return instance
    }
}