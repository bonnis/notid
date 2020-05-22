package com.example.notid

import android.app.Application
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiMain(context: Context){

        val prefs = Util().getPref(context)
        val client = OkHttpClient().newBuilder().addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        })
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor(prefs))
            .build()

        val retrofit = Retrofit.Builder().baseUrl("http://192.168.0.12:8001").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val services = retrofit.create(ApiServices::class.java)
}


