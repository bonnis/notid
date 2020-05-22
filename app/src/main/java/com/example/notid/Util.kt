package com.example.notid

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

final class Util {
    
    fun getPref(context:Context): SharedPreferences {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return  EncryptedSharedPreferences.create(context.getString(R.string.shared_pref_id),
            masterKey, context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }

    fun retrofitRequest(context: Context): ApiServices {
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

        val retrofit = Retrofit.Builder().baseUrl("http://192.168.137.1:8001").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
        return retrofit.create(ApiServices::class.java)
    }

    fun networkError(context: Context){
        Toast.makeText(context, "Network error, check connection", Toast.LENGTH_SHORT).show()
    }

    fun toastMaker(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
