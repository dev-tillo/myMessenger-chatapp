package com.example.mymessenger.utilmessnger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mymessenger.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessagingServ"
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.d(TAG, "onMessageReceived: ${p0.notification?.title}")
        Log.d(TAG, "onMessageReceived: ${p0.notification?.body}")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "channelId")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(p0.notification?.title)
        builder.setContentText(p0.notification?.body)

        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("channelId", "Name", importance)
            mChannel.description = "descriptionText"
            notificationManager.createNotificationChannel(mChannel)
        }
        notificationManager.notify(1, notification)
    }
}