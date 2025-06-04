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
import com.dainsleif.gaggy.notifications.NotificationChannelManager
import com.dainsleif.gaggy.util.WorkManagerUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Custom Application class for GAGgy
 */
class GaggyApplication : Application(), Configuration.Provider {
    
    private val TAG = "GaggyApplication"
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Initializing application")
        
        // Initialize Firebase
        initializeFirebase()
        
        // Create notification channels
        NotificationChannelManager(this).createAllChannels()
        
        // Initialize WorkManager for background processing
        initializeWorkManager()
    }
    
    /**
     * Initialize Firebase and Firebase Cloud Messaging
     */
    private fun initializeFirebase() {
        Log.d(TAG, "Initializing Firebase")
        FirebaseApp.initializeApp(this)
        
        // Subscribe to announcements topic
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to announcements topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to announcements topic", task.exception)
                }
            }
        
        // Get and log the FCM token
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "FCM Token: $token")
                    // You might want to send this token to your server
                } else {
                    Log.e(TAG, "Failed to get FCM token", task.exception)
                }
            }
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