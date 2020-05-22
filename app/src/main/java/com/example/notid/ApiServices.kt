package com.example.notid

import com.example.notid.model.Note
import com.example.notid.request.Credentials
import com.example.notid.request.DeltaRequest
import com.example.notid.request.FCMTokenRequest
import com.example.notid.request.Register
import com.example.notid.response.InfoResponse
import com.example.notid.response.LoginResponse
import com.example.notid.response.MessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiServices {
    @POST("auth")
    fun login(@Body info: Credentials?): Call<LoginResponse>

    @GET("info")
    fun getInfo(): Call<InfoResponse>

    @POST("register")
    fun register(@Body cred:Register): Call<MessageResponse>

    @POST("logout")
    fun logout(): Call<MessageResponse>

    @GET("note")
    fun getNote(): Call<List<Note>>

    @GET("note/{notecode}")
    fun getNote(@Path("notecode") notecode: String) : Call<Note>

    @GET("note/{notecode}?existence=true")
    fun checkNote(@Path("notecode") notecode : String): Call<MessageResponse>

    @POST("note")
    fun createNote(@Body note: Note) : Call<MessageResponse>

    @POST("transform/{id}")
    fun mergeRequest(@Path("id") id:Number, @Body delta:DeltaRequest) : Call<MessageResponse>

    @POST("fcm_token")
    fun register_fcm(@Body token:FCMTokenRequest) : Call<MessageResponse>
}