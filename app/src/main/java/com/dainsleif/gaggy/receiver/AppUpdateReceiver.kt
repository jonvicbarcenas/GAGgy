package com.dainsleif.gaggy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dainsleif.gaggy.service.DeviceOnlineService

/**
 * Receiver for MY_PACKAGE_REPLACED intent to detect when the app is updated
 * This ensures we re-register the device after updates
 */
class AppUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // App was updated, re-register the device
            DeviceOnlineService.registerDevice(context)
        }
    }
} 