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
import com.dainsleif.gaggy.data.models.Weather
import com.dainsleif.gaggy.receivers.NotificationStopReceiver
import com.dainsleif.gaggy.ui.main.MainActivity

/**
 * Helper class for creating and managing notifications
 */
class NotificationHelper private constructor(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val mediaManager = MediaManager.getInstance(context)
    private var activeNotificationId: Int? = null
    
    // Keep track of pending items for batch notifications
    private val pendingItems = mutableMapOf<ItemType, MutableList<Item>>()
    
    companion object {
        const val ACTION_STOP_NOTIFICATION = "com.dainsleif.gaggy.STOP_NOTIFICATION"
        private const val TAG = "NotificationHelper"
        private const val ANNOUNCEMENT_NOTIFICATION_ID = 9999
        private const val BATCH_NOTIFICATION_DELAY_MS = 500L // Delay to batch notifications
        
        // Notification IDs for batch notifications
        private const val GEAR_BATCH_NOTIFICATION_ID = 1001
        private const val SEED_BATCH_NOTIFICATION_ID = 2001
        private const val EGG_BATCH_NOTIFICATION_ID = 3001
        private const val WEATHER_BATCH_NOTIFICATION_ID = 4001
        
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
        
        // First, stop any existing notifications to prevent double sound
        stopAllNotifications()
        
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
     * Add item to batch and create notification after a short delay
     */
    fun createItemNotification(item: Item) {
        Log.d(TAG, "Adding item to batch: ${item.name}")
        
        // Add the item to the pending list for its type
        synchronized(pendingItems) {
            if (!pendingItems.containsKey(item.type)) {
                pendingItems[item.type] = mutableListOf()
            }
            pendingItems[item.type]?.add(item)
        }
        
        // Schedule a delayed notification to batch multiple items
        android.os.Handler(context.mainLooper).postDelayed({
            processPendingItems(item.type)
        }, BATCH_NOTIFICATION_DELAY_MS)
    }
    
    /**
     * Process all pending items of a specific type and create a batch notification
     */
    private fun processPendingItems(itemType: ItemType) {
        val items: List<Item>
        
        // Get and clear the pending items atomically
        synchronized(pendingItems) {
            items = pendingItems[itemType]?.toList() ?: listOf()
            pendingItems[itemType]?.clear()
        }
        
        if (items.isEmpty()) {
            return
        }
        
        Log.d(TAG, "Creating batch notification for ${items.size} ${itemType.name} items")
        
        // First, stop any existing notifications to prevent double sound
        stopAllNotifications()
        
        val channelId = when (itemType) {
            ItemType.GEAR -> NotificationChannelManager.GEAR_CHANNEL_ID
            ItemType.SEED -> NotificationChannelManager.SEED_CHANNEL_ID
            ItemType.EGG -> NotificationChannelManager.EGG_CHANNEL_ID
            else -> NotificationChannelManager.GEAR_CHANNEL_ID
        }
        
        // Create notification builder for batch
        val builder = createBatchNotificationBuilder(items, itemType, channelId)
        
        // Add flags to ensure alert is shown
        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        // Send the notification
        val notificationId = getBatchNotificationId(itemType)
        notificationManager.notify(notificationId, notification)
        activeNotificationId = notificationId
        
        // Play sound and start vibration
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.urgent}")
        mediaManager.playNotificationSound(soundUri)
        mediaManager.startVibration(10000L) // 10 seconds
        
        Log.d(TAG, "Batch notification created for ${items.size} ${itemType.name} items with ID: $notificationId")
    }
    
    /**
     * Create a notification for a weather event
     */
    fun createWeatherNotification(weather: Weather) {
        Log.d(TAG, "Creating notification for weather: ${weather.title}")
        
        // First, stop any existing notifications to prevent double sound
        stopAllNotifications()
        
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
            WEATHER_BATCH_NOTIFICATION_ID,
            intent,
            pendingIntentFlags
        )
        
        // Create a stop action intent
        val stopIntent = Intent(context, NotificationStopReceiver::class.java).apply {
            action = ACTION_STOP_NOTIFICATION
            putExtra("notification_id", WEATHER_BATCH_NOTIFICATION_ID)
            putExtra("item_name", weather.title)
            putExtra("item_type", ItemType.WEATHER.ordinal)
        }
        
        // Use FLAG_CANCEL_CURRENT to ensure the intent is recreated each time
        val stopPendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
        
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            WEATHER_BATCH_NOTIFICATION_ID + 2000, // Different ID to avoid conflicts
            stopIntent,
            stopPendingIntentFlags
        )
        
        // Create the notification
        val builder = NotificationCompat.Builder(context, NotificationChannelManager.WEATHER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(weather.title)
            .setContentText(weather.description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(weather.description))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            // Do not set sound or vibration in notification (we handle it manually)
            .setVibrate(null) // No vibration pattern
            .setDefaults(0) // No defaults
            .setSound(null) // No sound
            .setOngoing(true) // Make it persistent until explicitly dismissed
            // Add stop action with a clear icon and text
            .addAction(android.R.drawable.ic_delete, "STOP", stopPendingIntent)
            // Ensure notification pops up on screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        
        // Add flags to ensure alert is shown
        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        // Send the notification
        notificationManager.notify(WEATHER_BATCH_NOTIFICATION_ID, notification)
        activeNotificationId = WEATHER_BATCH_NOTIFICATION_ID
        
        // Play sound and start vibration
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.urgent}")
        mediaManager.playNotificationSound(soundUri)
        mediaManager.startVibration(10000L) // 10 seconds
        
        Log.d(TAG, "Notification created for weather: ${weather.title} with ID: $WEATHER_BATCH_NOTIFICATION_ID")
    }
    
    /**
     * Stop notification with given ID
     */
    fun stopNotification(notificationId: Int) {
        Log.d(TAG, "Stopping notification: $notificationId")
        try {
            // First stop all media
            mediaManager.stopAllMedia()
            
            // Cancel the specific notification
            notificationManager.cancel(notificationId)
            
            // Also cancel all notifications as a failsafe
            notificationManager.cancelAll()
            
            // Reset active notification ID
            if (activeNotificationId == notificationId) {
                activeNotificationId = null
            }
            
            Log.d(TAG, "Successfully stopped notification: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping notification: ${e.message}")
            e.printStackTrace()
            
            // Try again with a different approach
            try {
                notificationManager.cancelAll()
                Log.d(TAG, "Canceled all notifications as fallback")
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling all notifications: ${e.message}")
            }
        }
    }
    
    /**
     * Stop all notifications and their effects
     */
    fun stopAllNotifications() {
        Log.d(TAG, "Stopping all notifications")
        try {
            // Stop all media first
            mediaManager.stopAllMedia()
            
            // Cancel any active notification
            activeNotificationId?.let {
                notificationManager.cancel(it)
                Log.d(TAG, "Canceled notification with ID: $it")
            }
            
            // Cancel all notifications as a failsafe
            notificationManager.cancelAll()
            
            // Reset active notification ID
            activeNotificationId = null
            
            Log.d(TAG, "Successfully stopped all notifications")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping all notifications: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Create a notification builder for a batch of items
     */
    private fun createBatchNotificationBuilder(items: List<Item>, itemType: ItemType, channelId: String): NotificationCompat.Builder {
        // Create an intent to open the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val notificationId = getBatchNotificationId(itemType)
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            pendingIntentFlags
        )
        
        // Create a full screen intent for high priority notifications
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            notificationId + 1000, // Different ID to avoid conflicts
            intent,
            pendingIntentFlags
        )
        
        // Create a stop action intent
        val stopIntent = Intent(context, NotificationStopReceiver::class.java).apply {
            action = ACTION_STOP_NOTIFICATION
            putExtra("notification_id", notificationId)
            putExtra("item_name", getItemTypeDisplayName(itemType))
            putExtra("item_type", itemType.ordinal)
        }
        
        // Use FLAG_CANCEL_CURRENT to ensure the intent is recreated each time
        val stopPendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
        
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2000, // Different ID to avoid conflicts
            stopIntent,
            stopPendingIntentFlags
        )
        
        // Create title and content based on items
        val title = "${getItemTypeDisplayName(itemType)} Available!"
        val content = buildItemListText(items)
        
        Log.d(TAG, "Created batch notification: $title - $content")
        
        // Create the notification
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
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
            // Add stop action with a clear icon and text
            .addAction(android.R.drawable.ic_delete, "STOP", stopPendingIntent)
            // Ensure notification pops up on screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }
    
    /**
     * Build text listing all items in a batch
     */
    private fun buildItemListText(items: List<Item>): String {
        if (items.isEmpty()) return ""
        if (items.size == 1) return "${items[0].name} is now available with quantity: ${items[0].quantity}"
        
        val builder = StringBuilder()
        builder.append("The following items are now available:\n")
        
        items.forEach { item ->
            builder.append("• ${item.name}: ${item.quantity}\n")
        }
        
        return builder.toString().trim()
    }
    
    /**
     * Get the display name for an item type
     */
    private fun getItemTypeDisplayName(itemType: ItemType): String {
        return when (itemType) {
            ItemType.GEAR -> "Gear"
            ItemType.SEED -> "Seeds"
            ItemType.EGG -> "Eggs"
            ItemType.WEATHER -> "Weather"
            else -> "Items" // Default case for unknown item types
        }
    }
    
    /**
     * Get notification ID for a batch based on item type
     */
    private fun getBatchNotificationId(itemType: ItemType): Int {
        return when (itemType) {
            ItemType.GEAR -> GEAR_BATCH_NOTIFICATION_ID
            ItemType.SEED -> SEED_BATCH_NOTIFICATION_ID
            ItemType.EGG -> EGG_BATCH_NOTIFICATION_ID
            ItemType.WEATHER -> WEATHER_BATCH_NOTIFICATION_ID
            else -> GEAR_BATCH_NOTIFICATION_ID // Default to gear ID for unknown types
        }
    }
    
    // Generate a unique notification ID for each item
    private fun getNotificationId(itemName: String): Int {
        return itemName.hashCode()
    }
} 