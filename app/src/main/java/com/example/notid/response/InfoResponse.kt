package com.example.notid.response

import com.example.notid.model.User
import com.google.gson.annotations.SerializedName

data class InfoResponse(
    @SerializedName("user")
    val user: User?=null
) {
}