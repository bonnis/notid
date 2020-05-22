package com.example.notid.request

import com.example.notid.model.User
import com.google.gson.annotations.SerializedName

class Register (
    @SerializedName("email")
    var email: String?=null,

    @SerializedName("first_name")
    var first_name: String?=null,

    @SerializedName("last_name")
    var last_name: String?=null,

    @SerializedName("password")
    var password: String?=null
){
}