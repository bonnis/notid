package com.example.notid

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(var prefs : SharedPreferences) : Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.getString("login_token", "")
        var request = chain.request()
        if (token!="") {
            request = request?.newBuilder()
                ?.addHeader("Authorization", "Bearer " + token!!.toString())
                ?.build()
        }
        return chain.proceed(request)
    }
}

