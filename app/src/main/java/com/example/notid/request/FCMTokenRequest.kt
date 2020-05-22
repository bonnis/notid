package com.example.notid.request

import com.google.gson.annotations.SerializedName

class FCMTokenRequest (
    @SerializedName("token")
    var token : String
)