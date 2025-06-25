package com.dainsleif.gaggy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dainsleif.gaggy.notifications.NotificationHelper

/**
 * BroadcastReceiver to handle stopping notifications
 */
class NotificationStopReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationStopReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "NotificationStopReceiver received: ${intent.action}")
        
        if (intent.action == NotificationHelper.ACTION_STOP_NOTIFICATION) {
            // Get notification ID from intent
            val notificationId = intent.getIntExtra("notification_id", -1)
            val itemName = intent.getStringExtra("item_name") ?: ""
            
            Log.d(TAG, "Stopping notification: ID=$notificationId, name=$itemName")
            
            // Show toast to confirm action
            showToast(context, "Stopping notification for: $itemName")
            
            // Use the NotificationHelper singleton to stop the notification
            val notificationHelper = NotificationHelper.getInstance(context)
            
            // First try to stop all notifications as a failsafe
            notificationHelper.stopAllNotifications()
            
            if (notificationId != -1) {
                // Then specifically stop this notification
                notificationHelper.stopNotification(notificationId)
                Log.d(TAG, "Notification stopped: $notificationId for item: $itemName")
            } else {
                Log.d(TAG, "Invalid notification ID, stopping all notifications")
            }
        }
    }
    
    private fun showToast(context: Context, message: String) {
        try {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast: ${e.message}")
        }
    }
} 