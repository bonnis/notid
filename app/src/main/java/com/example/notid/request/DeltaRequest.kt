package com.example.notid.request

import com.google.gson.annotations.SerializedName

class DeltaRequest(
    @SerializedName("delta")
    val delta:String
) {

}