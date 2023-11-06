package com.sujitbhoir.campusdiary.notification

import android.app.admin.PolicyUpdateReceiver
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.sujitbhoir.campusdiary.dataclasses.UserData
import com.sujitbhoir.campusdiary.datahandlers.NotificationManager
import com.sujitbhoir.campusdiary.datahandlers.UsersManager

class NotificationService  : FirebaseMessagingService() {

    val TAG = "notificationTAG"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val title  = remoteMessage.notification?.title!!
            val content = remoteMessage.notification?.body!!
            val data = Gson().toJson(remoteMessage.data)
            val gson = Gson()
            val m = gson.fromJson(data, MessageBody::class.java)

            if (m.receiverId == UsersManager(this).getMyData()!!.id || m.receiverId == "all")
            {
                NotificationManager(this).showNotification(this, title, content)
            }



            Log.d(TAG, "Token was updated data is $data")



        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        Log.d(TAG, "token was updated")
        val data = hashMapOf<String, Any>(
            "notificationToken" to token
        )
       Firebase.firestore.collection("users").document(Firebase.auth.currentUser!!.uid).set(data, SetOptions.merge()).addOnSuccessListener { Log.d(TAG, "token was updated") }
    }
}

data class MessageBody(
    var receiverId : String = ""
)