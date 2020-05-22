package com.example.notid.model

import com.google.gson.annotations.SerializedName

class User(
    @SerializedName("id")
    var id: Number?=null,

    @SerializedName("email")
    var email: String?=null,

    @SerializedName("first_name")
    var first_name: String?=null,

    @SerializedName("last_name")
    var last_name: String?=null
) {
}