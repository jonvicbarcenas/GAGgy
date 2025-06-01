package com.dainsleif.gaggy.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Handles creation and management of notification channels
 */
class NotificationChannelManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val GEAR_CHANNEL_ID = "gear_channel"
        const val SEED_CHANNEL_ID = "seed_channel"
        const val EGG_CHANNEL_ID = "egg_channel"
        private const val TAG = "NotificationChannelManager"
    }
    
    /**
     * Creates all required notification channels for the app
     */
    fun createAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createGearChannel()
            createSeedChannel()
            createEggChannel()
            Log.d(TAG, "All notification channels created")
        }
    }
    
    private fun createGearChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Gear Notifications"
            val description = "Notifications for available gears"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(GEAR_CHANNEL_ID, name, importance).apply {
                this.description = description
                
                // Enable lights and make it show as alert on lockscreen
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                
                // Disable channel vibration, we'll handle it manually
                enableVibration(false)
                
                // Disable sound on channel to prevent double sounds
                setSound(null, null)
                
                // Allow channel to bypass Do Not Disturb mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setAllowBubbles(true)
                }
                
                // Show badge
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Gear notification channel created")
        }
    }
    
    private fun createSeedChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Seed Notifications"
            val description = "Notifications for available seeds"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(SEED_CHANNEL_ID, name, importance).apply {
                this.description = description
                
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setAllowBubbles(true)
                }
                
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Seed notification channel created")
        }
    }
    
    private fun createEggChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Egg Notifications"
            val description = "Notifications for available eggs"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(EGG_CHANNEL_ID, name, importance).apply {
                this.description = description
                
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setAllowBubbles(true)
                }
                
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Egg notification channel created")
        }
    }
} 