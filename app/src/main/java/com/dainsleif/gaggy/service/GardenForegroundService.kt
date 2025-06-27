package com.dainsleif.gaggy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dainsleif.gaggy.MainActivity
import com.dainsleif.gaggy.R

class GardenForegroundService : Service() {
    
    private lateinit var notificationSoundService: NotificationSoundService
    
    companion object {
        private const val FOREGROUND_SERVICE_ID = 1002
        private const val FOREGROUND_CHANNEL_ID = "garden_foreground_channel"
        
        fun startService(context: Context) {
            val startIntent = Intent(context, GardenForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
        }
        
        fun stopService(context: Context) {
            val stopIntent = Intent(context, GardenForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        notificationSoundService = NotificationSoundService(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(FOREGROUND_SERVICE_ID, createForegroundNotification())
        
        // Return sticky to ensure the service restarts if killed
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up resources
        if (::notificationSoundService.isInitialized) {
            notificationSoundService.release()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Garden Background Service"
            val descriptionText = "Background service for monitoring garden stocks"
            val importance = NotificationManager.IMPORTANCE_LOW // Low importance to be less intrusive
            val channel = NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Garden Stock Checker")
            .setContentText("Automatically checking for new garden stocks...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
} 