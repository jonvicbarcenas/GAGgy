package com.dainsleif.gaggy

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.dainsleif.gaggy.util.WorkManagerUtil

/**
 * Custom Application class for GAGgy
 */
class GaggyApplication : Application(), Configuration.Provider {
    
    private val TAG = "GaggyApplication"
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Initializing application")
        
        // Initialize WorkManager for background processing
        initializeWorkManager()
    }
    
    /**
     * Initialize WorkManager to schedule background tasks
     */
    private fun initializeWorkManager() {
        Log.d(TAG, "Initializing WorkManager")
        
        // Schedule the stock monitoring worker
        WorkManagerUtil.scheduleStockMonitoring(this)
    }
    
    /**
     * Request battery optimization exemption
     * This should be called from MainActivity
     */
    fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            val packageName = packageName
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = Uri.parse("package:$packageName")
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to request battery optimization exemption: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Provide WorkManager configuration
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
} 