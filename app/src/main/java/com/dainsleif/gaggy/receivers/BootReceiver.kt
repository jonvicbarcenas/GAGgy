package com.dainsleif.gaggy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dainsleif.gaggy.util.ForegroundService

/**
 * BroadcastReceiver to start the service when the device boots
 */
class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting foreground service")
            ForegroundService.startService(context)
        }
    }
} 