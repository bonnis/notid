package com.example.notid

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("newToken", token)
        //Add your token in your sharepreferences.
        Util().getPref(applicationContext).edit()
            .putString("fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

    }

    companion object {
        //Whenewer you need FCM token, just call this static method to get it.
        fun getToken(context: Context): String {
            return Util().getPref(context)
                .getString("fcm_token", "empty")
        }
    }
}
