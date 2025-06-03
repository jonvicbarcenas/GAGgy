package com.dainsleif.gaggy.util

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Utility class to manage WorkManager operations
 */
object WorkManagerUtil {
    private const val TAG = "WorkManagerUtil"
    private const val STOCK_MONITOR_WORK_NAME = "stock_monitor_work"
    
    /**
     * Schedule the stock monitoring worker to run periodically
     */
    fun scheduleStockMonitoring(context: Context) {
        Log.d(TAG, "Scheduling stock monitoring worker")
        
        // Define constraints - require network connection
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()
        
        // Create a periodic work request that runs every 15 minutes
        // The minimum interval is 15 minutes for API level 23+
        val workRequest = PeriodicWorkRequestBuilder<StockMonitorWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30, // 30 seconds initial backoff
                TimeUnit.SECONDS
            )
            .addTag("stock_monitoring")
            .build()
        
        // Enqueue the work request, replacing any existing one
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            STOCK_MONITOR_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        Log.d(TAG, "Stock monitoring worker scheduled")
        
        // Also run it once immediately
        val oneTimeWorkRequest = PeriodicWorkRequestBuilder<StockMonitorWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(0, TimeUnit.SECONDS)
            .addTag("stock_monitoring_immediate")
            .build()
            
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
        Log.d(TAG, "Immediate stock monitoring worker scheduled")
    }
    
    /**
     * Cancel the stock monitoring worker
     */
    fun cancelStockMonitoring(context: Context) {
        Log.d(TAG, "Cancelling stock monitoring worker")
        WorkManager.getInstance(context).cancelUniqueWork(STOCK_MONITOR_WORK_NAME)
    }
} 