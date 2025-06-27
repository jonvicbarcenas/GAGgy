package com.dainsleif.gaggy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dainsleif.gaggy.service.GardenForegroundService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the foreground service when the device boots
            GardenForegroundService.startService(context)
        }
    }
} 