package com.dainsleif.gaggy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dainsleif.gaggy.data.ItemRepository
import com.dainsleif.gaggy.data.models.ItemType
import com.dainsleif.gaggy.notifications.NotificationHelper

/**
 * BroadcastReceiver to handle stopping notifications
 */
class NotificationStopReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationStopReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_STOP_NOTIFICATION) {
            // Get notification ID from intent
            val notificationId = intent.getIntExtra("notification_id", -1)
            val itemName = intent.getStringExtra("item_name") ?: ""
            val itemTypeOrdinal = intent.getIntExtra("item_type", -1)
            
            if (notificationId != -1 && itemName.isNotEmpty() && itemTypeOrdinal >= 0) {
                // Use the NotificationHelper singleton to stop the notification
                val notificationHelper = NotificationHelper.getInstance(context)
                notificationHelper.stopNotification(notificationId)
                
                // Save the last updated time
                val itemRepository = ItemRepository.getInstance(context)
                val itemType = ItemType.values()[itemTypeOrdinal]
                val lastUpdated = itemRepository.getCurrentLastUpdatedTime(itemType)
                
                if (lastUpdated.isNotEmpty()) {
                    itemRepository.saveItemLastUpdatedTime(itemName, lastUpdated)
                    Log.d(TAG, "Saved last updated time for $itemName: $lastUpdated")
                }
                
                Log.d(TAG, "Notification stopped: $notificationId for item: $itemName")
            } else {
                // Fallback to just stopping notification without saving time
                if (notificationId != -1) {
                    val notificationHelper = NotificationHelper.getInstance(context)
                    notificationHelper.stopNotification(notificationId)
                    Log.d(TAG, "Notification stopped: $notificationId (no item info)")
                }
            }
        }
    }
} 