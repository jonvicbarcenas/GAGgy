package com.dainsleif.gaggy.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.ui.main.MainActivity

/**
 * Foreground service to keep the app running in the background
 */
class ForegroundService : Service() {
    
    companion object {
        private const val TAG = "ForegroundService"
        private const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "gaggy_foreground_channel"
        const val ACTION_START_SERVICE = "com.dainsleif.gaggy.ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.dainsleif.gaggy.ACTION_STOP_SERVICE"
        
        /**
         * Start the foreground service
         */
        fun startService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Service start requested")
        }
        
        /**
         * Stop the foreground service
         */
        fun stopService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
            Log.d(TAG, "Service stop requested")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                Log.d(TAG, "Starting foreground service")
                try {
                    startForeground(NOTIFICATION_ID, createNotification())
                    Log.d(TAG, "Foreground service started successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting foreground service: ${e.message}")
                    e.printStackTrace()
                }
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Stopping foreground service")
                stopForeground(true)
                stopSelf()
            }
        }
        
        // If service is killed, restart it
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
    
    /**
     * Create the notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "GAGgy Background Service"
            val descriptionText = "Keeps GAGgy running to check for item updates"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }
    
    /**
     * Create the persistent notification
     */
    private fun createNotification(): Notification {
        // Create intent to open the app when notification is tapped
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GAGgy is running")
            .setContentText("Monitoring for item updates")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
} 