package com.example.mymessenger.keraksiz.notification

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFireBaceIdService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: " + remoteMessage.from)
        Log.d(TAG, "Message data payload: " + remoteMessage.data)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        var firebaceuser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
//
////        var refreshtoken: String = FirebaseAuth()
//
//        Log.i("SellerFirebaseService ", "Refreshed token :: $token")
//        sendRegistrationToServer(token)
//    }
//
//    private fun sendRegistrationToServer(token: String) {
//
//    }
//
//    override fun onMessageReceived(message: RemoteMessage) {
//        super.onMessageReceived(message)
//        Log.i("SellerFirebaseService ", "Message :: $message")
//    }
}