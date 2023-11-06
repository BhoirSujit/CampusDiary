package com.sujitbhoir.campusdiary.datahandlers

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sujitbhoir.campusdiary.FlashScreen
import com.sujitbhoir.campusdiary.MainActivity
import com.sujitbhoir.campusdiary.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Random


class NotificationManager(val context : Context) {

    val TAG = "NotificationManagerTAG"
    fun isAppRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == "com.sujitbhoir.campusdiary") {
                    return true
                }
            }
        }
        return false
    }

    fun showMessageNotification(context : Context, title : String, body : String)
    {
        val builder = NotificationCompat.Builder(context, "sujitbhoir.campusdiary")

        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setSmallIcon(R.drawable.campus_diary_logo)
        builder.priority = NotificationCompat.PRIORITY_MAX

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )


        builder.setContentIntent(contentIntent)

        if (!isAppRunning(context))
        {

        }



        val channelId = "sujitbhoir.campusdiary.id"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "CampusDiary Channel", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
        builder.setChannelId(channelId)




        manager.notify(Random().nextInt(), builder.build())
    }

    fun showNotification(context : Context, title : String, body : String)
    {
        val builder = NotificationCompat.Builder(context, "sujitbhoir.campusdiary")

        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setSmallIcon(R.drawable.campus_diary_logo)
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setAutoCancel(true)

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, FlashScreen::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )


        builder.setContentIntent(contentIntent)

        if (!isAppRunning(context))
        {

        }



        val channelId = "sujitbhoir.campusdiary.id"

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "CampusDiary Channel", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
        builder.setChannelId(channelId)




        manager.notify(Random().nextInt(), builder.build())
    }


    private fun sendMessage(receiverToken : String,yourName:String, yourMessage: Message, receiverID : String)
    {

        val json = """
    {
        "to": "$receiverToken",
        "notification": {
          "title": "Request Accepted",
          "body": "You request has been approved by $yourName"
        },
        "data": {
          "senderId": "$receiverID"
        }
    }
    """.trimIndent()

        callApi(json)
    }

    fun sendAcceptionACK(receiverToken : String,yourName:String, receiverID : String)
    {

        Log.d(TAG, "receivertoken : $receiverToken")
        val json = """
    {
        "to": "$receiverToken",
        "notification": {
          "title": "Request Accepted",
          "body": "You request has been approved by $yourName"
        },
        "data": {
          "receiverId": "$receiverID"
        }
    }
    """.trimIndent()

        callApi(json)
    }

    fun sendSessionRequest(requestContext : String, yourname : String ,receiverToken : String, receiverId : String)
    {

        val json = """
    {
        "to": "$receiverToken",
        "notification": {
          "title": "You have new Request",
          "body": "$yourname : $requestContext"
        },
        "data": {
          "receiverId": "$receiverId"
        }
    }
    """.trimIndent()

        callApi(json)
    }



    fun callApi(json : String)
    {
        // Replace with your actual FCM server key
        val YOUR_FCM_SERVER_KEY = "AAAABDRd4Ek:APA91bFdKvu5RqFhZ7_v4MnDH9WNzq69PzZFntbFu6trq_ndDdlWf9kqoBEPqf4YjOFc_2LpCXdfj5f0y_-pIsADw5dCP0AjEkWZHYKyVgOTnoO1S3HFnwVRFCAW2Q3lMMbKKgcbwD_f"//UsersManager(context).getMyData()!!.notificationToken
        Log.d(TAG, "fcm token is : $YOUR_FCM_SERVER_KEY")


        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val url = "https://fcm.googleapis.com/fcm/send"

        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Authorization", "key=$YOUR_FCM_SERVER_KEY")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error sending FCM message: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e(TAG, "Error sending FCM message: ${response.message}")
            }

        })
    }
}