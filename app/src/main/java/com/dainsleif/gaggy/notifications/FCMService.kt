package com.dainsleif.gaggy.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // You might want to send this token to your server
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            
            // Get the notification helper to display the notification
            val notificationHelper = NotificationHelper.getInstance(this)
            
            val title = notification.title ?: "GAGgy"
            val message = notification.body ?: ""
            
            // Show notification
            notificationHelper.showAnnouncementNotification(title, message)
        }
        
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            // Process data payload if needed
            // This could contain custom data for your app
        }
    }
} 