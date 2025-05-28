package com.dainsleif.gaggy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_STOP_NOTIFICATION) {
            // Get notification ID from intent
            val notificationId = intent.getIntExtra("notification_id", -1)
            
            if (notificationId != -1) {
                // Use the NotificationHelper singleton to stop the notification
                val notificationHelper = NotificationHelper.getInstance(context)
                notificationHelper.stopNotification(notificationId)
                Log.d("NotificationStopReceiver", "Notification stopped: $notificationId")
            }
        }
    }
} 