package com.example.notid.model

import com.google.gson.annotations.SerializedName
import java.math.BigInteger
import java.util.*

class Note(
    @SerializedName("id")
    var id : Number?=null,
    @SerializedName("text")
    var text : String?=null,
    @SerializedName("name")
    var name : String?=null,
    @SerializedName("notecode")
    var noteCode : String?=null,
    @SerializedName("is_private")
    var isPrivate : Boolean?=null,
    @SerializedName("user_allowed")
    var permission: List<UserPermission>?=null,
    @SerializedName("updated_at")
    var date: Date?=null,
    @SerializedName("version")
    var version : BigInteger?=null
    ) {
}

class UserPermission(
    @SerializedName("user")
    var id : Number?=null,
    @SerializedName("readonly")
    var readOnly : Boolean?=null
)