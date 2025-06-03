package com.dainsleif.gaggy.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.data.ItemRepository
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.data.models.ItemType
import com.dainsleif.gaggy.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Worker class for monitoring stock changes in the background
 */
class StockMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val TAG = "StockMonitorWorker"
    private val itemRepository = ItemRepository.getInstance(context)
    private val notificationHelper = NotificationHelper.getInstance(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val WORK_NAME = "stock_monitor_work"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "stock_monitor_channel"
        private const val CHANNEL_NAME = "Stock Monitoring"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "StockMonitorWorker started")
        
        try {
            // Set as foreground service with notification
            setForeground(createForegroundInfo("Monitoring stock changes..."))
            
            // Check for new items that need notifications
            checkForNewItems()
            
            // Worker completed successfully
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in worker: ${e.message}")
            Result.retry()
        }
    }
    
    /**
     * Create foreground notification info
     */
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        // Create notification channel if needed
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("GAGgy Stock Monitor")
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
            
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
    
    /**
     * Create notification channel for foreground service
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for monitoring stock changes in the background"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Check for new items that need notifications
     */
    private suspend fun checkForNewItems() {
        Log.d(TAG, "Checking for new items")
        
        val gearItems = itemRepository.getGearItems()
        val seedItems = itemRepository.getSeedItems()
        val eggItems = itemRepository.getEggItems()
        val honeyItems = itemRepository.getHoneyItems()
        
        // Check each type of item
        checkForNotifications(gearItems, ItemType.GEAR)
        checkForNotifications(seedItems, ItemType.SEED)
        checkForNotifications(eggItems, ItemType.EGG)
        checkForNotifications(honeyItems, ItemType.HONEY)
        
        // Wait a bit to ensure Firebase data is processed
        delay(5000)
    }
    
    /**
     * Check if any items need notifications
     */
    private fun checkForNotifications(items: List<Item>, itemType: ItemType) {
        items.forEach { item ->
            // If quantity is greater than 0 and notifications are enabled for this item, send notification
            if (item.quantity > 0 && itemRepository.isNotificationEnabled(item.name)) {
                Log.d(TAG, "Sending notification for ${item.name} with quantity ${item.quantity}")
                notificationHelper.createItemNotification(item)
            }
        }
    }
}