package com.example.danp_lab07.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New Token: $token")
        // Este es el token que usarás en la consola de Firebase para enviar pruebas
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "From: ${message.from}")
        
        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // Show notification to user
        }
    }
}
