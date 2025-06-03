package com.dainsleif.gaggy.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Helper class to handle auto-start permissions on different device manufacturers
 */
class AutoStartHelper {
    
    private val TAG = "AutoStartHelper"
    
    companion object {
        @Volatile
        private var instance: AutoStartHelper? = null
        
        fun getInstance(): AutoStartHelper {
            return instance ?: synchronized(this) {
                instance ?: AutoStartHelper().also { instance = it }
            }
        }
    }
    
    /**
     * Get auto-start permission for the app
     * This is needed on some devices like Xiaomi, Oppo, Vivo, etc.
     */
    fun getAutoStartPermission(context: Context) {
        try {
            val manufacturer = Build.MANUFACTURER.lowercase()
            Log.d(TAG, "Device manufacturer: $manufacturer")
            
            val intent = when {
                // Xiaomi
                manufacturer.contains("xiaomi") -> {
                    Intent().apply {
                        component = ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    }
                }
                // Oppo
                manufacturer.contains("oppo") -> {
                    Intent().apply {
                        component = ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    }
                }
                // Vivo
                manufacturer.contains("vivo") -> {
                    Intent().apply {
                        component = ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    }
                }
                // Huawei
                manufacturer.contains("huawei") -> {
                    Intent().apply {
                        component = ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.optimize.process.ProtectActivity"
                        )
                    }
                }
                // Samsung
                manufacturer.contains("samsung") -> {
                    Intent().apply {
                        component = ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.ui.battery.BatteryActivity"
                        )
                    }
                }
                // OnePlus
                manufacturer.contains("oneplus") -> {
                    Intent().apply {
                        component = ComponentName(
                            "com.oneplus.security",
                            "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                        )
                    }
                }
                // No known auto-start settings for this manufacturer
                else -> null
            }
            
            if (intent != null) {
                Log.d(TAG, "Attempting to open auto-start settings for $manufacturer")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Log.d(TAG, "No known auto-start settings for $manufacturer")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open auto-start settings: ${e.message}")
        }
    }
} 