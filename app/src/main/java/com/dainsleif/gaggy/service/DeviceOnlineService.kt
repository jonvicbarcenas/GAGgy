package com.dainsleif.gaggy.service

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.dainsleif.gaggy.model.DeviceInfo
import com.google.firebase.database.FirebaseDatabase
import java.util.Timer
import java.util.TimerTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceOnlineService {
    companion object {
        private const val DEVICES_PATH = "datas/devices"
        private const val UPDATE_INTERVAL = 5 * 60 * 1000L // 5 minutes in milliseconds
        private var updateTimer: Timer? = null
        
        fun registerDevice(context: Context) {
            val deviceInfo = getDeviceInfo(context)
            
            // Save to Firebase
            saveDeviceInfo(deviceInfo)
            
            // Schedule regular updates of "lastOnline" timestamp
            scheduleOnlineUpdates(context)
        }
        
        private fun getDeviceInfo(context: Context): DeviceInfo {
            // Get unique Android ID (persists across app installs)
            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            // Get device name
            val deviceName = Build.MODEL
            
            // Get current time as lastOnline
            val lastOnline = System.currentTimeMillis()
            
            // Get app version
            val appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "unknown"
            }
            
            // Get OS version
            val osVersion = "Android ${Build.VERSION.RELEASE}"
            
            return DeviceInfo(
                deviceId = deviceId,
                deviceName = deviceName,
                lastOnline = lastOnline,
                appVersion = appVersion,
                osVersion = osVersion
            )
        }
        
        private fun saveDeviceInfo(deviceInfo: DeviceInfo) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = FirebaseDatabase.getInstance()
                    val reference = database.getReference("/$DEVICES_PATH/${deviceInfo.deviceId}")
                    reference.setValue(deviceInfo)
                } catch (e: Exception) {
                    // Log error
                    android.util.Log.e("DeviceTracking", "Error saving device info", e)
                }
            }
        }
        
        private fun scheduleOnlineUpdates(context: Context) {
            // Cancel any existing timer
            updateTimer?.cancel()
            
            // Create new timer for regular updates
            updateTimer = Timer()
            updateTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    updateLastOnline(context)
                }
            }, UPDATE_INTERVAL, UPDATE_INTERVAL)
        }
        
        private fun updateLastOnline(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val deviceId = Settings.Secure.getString(
                        context.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    
                    val lastOnline = System.currentTimeMillis()
                    
                    val database = FirebaseDatabase.getInstance()
                    val reference = database.getReference("/$DEVICES_PATH/$deviceId/lastOnline")
                    reference.setValue(lastOnline)
                    
                    // Also update app version in case it changed
                    try {
                        val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                        val versionRef = database.getReference("/$DEVICES_PATH/$deviceId/appVersion")
                        versionRef.setValue(appVersion)
                    } catch (e: Exception) {
                        // Log error
                        android.util.Log.e("DeviceTracking", "Error updating app version", e)
                    }
                } catch (e: Exception) {
                    // Log error
                    android.util.Log.e("DeviceTracking", "Error updating lastOnline", e)
                }
            }
        }
        
        fun stopTracking() {
            updateTimer?.cancel()
            updateTimer = null
        }
    }
} 