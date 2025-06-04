package com.dainsleif.gaggy.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.data.models.ItemType
import com.dainsleif.gaggy.receivers.NotificationStopReceiver
import com.dainsleif.gaggy.ui.main.MainActivity

/**
 * Helper class for creating and managing notifications
 */
class NotificationHelper private constructor(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mediaManager = MediaManager.getInstance(context)
    private var activeNotificationId: Int? = null
    
    companion object {
        const val ACTION_STOP_NOTIFICATION = "com.dainsleif.gaggy.STOP_NOTIFICATION"
        private const val TAG = "NotificationHelper"
        private const val ANNOUNCEMENT_NOTIFICATION_ID = 9999
        
        // Singleton instance
        @Volatile
        private var INSTANCE: NotificationHelper? = null
        
        // Get singleton instance
        fun getInstance(context: Context): NotificationHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Show an announcement notification from Firebase Cloud Messaging
     */
    fun showAnnouncementNotification(title: String, message: String) {
        Log.d(TAG, "Showing announcement notification: $title")
        
        // Create an intent to open the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            ANNOUNCEMENT_NOTIFICATION_ID,
            intent,
            pendingIntentFlags
        )
        
        // Create the notification
        val builder = NotificationCompat.Builder(context, NotificationChannelManager.ANNOUNCEMENTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Send the notification
        notificationManager.notify(ANNOUNCEMENT_NOTIFICATION_ID, builder.build())
    }
    
    /**
     * Create a notification for an item
     */
    fun createItemNotification(item: Item) {
        Log.d(TAG, "Creating notification for ${item.name}")
        
        // First, stop any existing notifications to prevent double sound
        stopAllNotifications()
        
        val channelId = when (item.type) {
            ItemType.GEAR -> NotificationChannelManager.GEAR_CHANNEL_ID
            ItemType.SEED -> NotificationChannelManager.SEED_CHANNEL_ID
            ItemType.EGG -> NotificationChannelManager.EGG_CHANNEL_ID
            else -> NotificationChannelManager.GEAR_CHANNEL_ID
        }
        
        // Create notification builder
        val builder = createNotificationBuilder(item, channelId)
        
        // Add flags to ensure alert is shown
        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        // Send the notification
        val notificationId = getNotificationId(item.name)
        notificationManager.notify(notificationId, notification)
        activeNotificationId = notificationId
        
        // Play sound and start vibration
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.urgent}")
        mediaManager.playNotificationSound(soundUri)
        mediaManager.startVibration(10000L) // 10 seconds
        
        Log.d(TAG, "Notification created for: ${item.name} with ID: $notificationId")
    }
    
    /**
     * Stop notification with given ID
     */
    fun stopNotification(notificationId: Int) {
        Log.d(TAG, "Stopping notification: $notificationId")
        mediaManager.stopAllMedia()
        notificationManager.cancel(notificationId)
        
        if (activeNotificationId == notificationId) {
            activeNotificationId = null
        }
    }
    
    /**
     * Stop all notifications and their effects
     */
    fun stopAllNotifications() {
        Log.d(TAG, "Stopping all notifications")
        mediaManager.stopAllMedia()
        activeNotificationId?.let {
            notificationManager.cancel(it)
        }
        activeNotificationId = null
    }
    
    private fun createNotificationBuilder(item: Item, channelId: String): NotificationCompat.Builder {
        // Create an intent to open the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            getNotificationId(item.name),
            intent,
            pendingIntentFlags
        )
        
        // Create a full screen intent for high priority notifications
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            getNotificationId(item.name) + 1000, // Different ID to avoid conflicts
            intent,
            pendingIntentFlags
        )
        
        // Create a stop action intent
        val stopIntent = Intent(context, NotificationStopReceiver::class.java).apply {
            action = ACTION_STOP_NOTIFICATION
            putExtra("notification_id", getNotificationId(item.name))
            putExtra("item_name", item.name)
            putExtra("item_type", item.type.ordinal)
        }
        
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            getNotificationId(item.name) + 2000, // Different ID to avoid conflicts
            stopIntent,
            pendingIntentFlags
        )
        
        // Create the notification
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${item.name} Available!")
            .setContentText("${item.name} is now available with quantity: ${item.quantity}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(false)
            // Do not set sound or vibration in notification (we handle it manually)
            .setVibrate(null) // No vibration pattern
            .setDefaults(0) // No defaults
            .setSound(null) // No sound
            .setOngoing(true) // Make it persistent until explicitly dismissed
            // Add stop action
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            // Ensure notification pops up on screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }
    
    // Generate a unique notification ID for each item
    private fun getNotificationId(itemName: String): Int {
        return itemName.hashCode()
    }
} 