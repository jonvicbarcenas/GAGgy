package com.dainsleif.gaggy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dainsleif.gaggy.util.WorkManagerUtil

/**
 * Broadcast receiver that gets triggered when the device boots
 * Used to restart our WorkManager tasks
 */
class BootCompletedReceiver : BroadcastReceiver() {
    private val TAG = "BootCompletedReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, rescheduling stock monitoring")
            
            // Reschedule the stock monitoring worker
            WorkManagerUtil.scheduleStockMonitoring(context)
        }
    }
} 