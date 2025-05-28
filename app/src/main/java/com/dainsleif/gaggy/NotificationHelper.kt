package com.dainsleif.gaggy

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import android.util.Log

class NotificationHelper private constructor(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val ACTION_STOP_NOTIFICATION = "com.dainsleif.gaggy.STOP_NOTIFICATION"
        // Static MediaPlayer instance shared across all instances
        private var mediaPlayer: MediaPlayer? = null
        // Singleton instance
        @Volatile
        private var INSTANCE: NotificationHelper? = null
        
        // Get singleton instance
        fun getInstance(context: Context): NotificationHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun createGearNotification(gearName: String, quantity: Int) {
        Log.d("NotificationHelper", "Creating notification for $gearName")
        
        // First, stop any existing notifications to prevent double sound
        stopAllNotifications()
        
        // Create an intent to open the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            getNotificationId(gearName),
            intent,
            pendingIntentFlags
        )
        
        // Set up custom sound
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.urgent}")
        
        // Play sound directly using MediaPlayer to ensure it plays even when screen is off
        playNotificationSound(soundUri)
        
        // Set up vibration pattern (10 seconds)
        val vibrationDuration = 10000L // 10 seconds in milliseconds
        
        // Start vibration
        startVibration(vibrationDuration)
        
        // Create a full screen intent for high priority notifications
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            getNotificationId(gearName) + 1000, // Different ID to avoid conflicts
            intent,
            pendingIntentFlags
        )
        
        // Create a stop action intent
        val stopIntent = Intent(context, NotificationStopReceiver::class.java).apply {
            action = ACTION_STOP_NOTIFICATION
            putExtra("notification_id", getNotificationId(gearName))
        }
        
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            getNotificationId(gearName) + 2000, // Different ID to avoid conflicts
            stopIntent,
            pendingIntentFlags
        )
        
        // Create the notification
        val notification = NotificationCompat.Builder(context, "gear_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$gearName Available!")
            .setContentText("$gearName is now available with quantity: $quantity")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(false)
            // Do not set sound or vibration in notification (we handle it manually)
            .setVibrate(null) // No vibration pattern
            .setDefaults(0) // No defaults
            .setSound(null) // No sound
            .setOngoing(true) // Make it persistent until explicitly dismissed
            // Add stop action
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            // Ensure notification pops up on screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        // Add flags to ensure alert is shown
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        // Send the notification
        notificationManager.notify(getNotificationId(gearName), notification)
        Log.d("NotificationHelper", "Notification created for: $gearName with ID: ${getNotificationId(gearName)}")
    }
    
    fun createSeedNotification(seedName: String, quantity: Int) {
        Log.d("NotificationHelper", "Creating notification for seed: $seedName")
        
        // First, stop any existing notifications to prevent double sound
        stopAllNotifications()
        
        // Create an intent to open the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            getNotificationId(seedName),
            intent,
            pendingIntentFlags
        )
        
        // Set up custom sound
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.urgent}")
        
        // Play sound directly using MediaPlayer to ensure it plays even when screen is off
        playNotificationSound(soundUri)
        
        // Set up vibration pattern (10 seconds)
        val vibrationDuration = 10000L // 10 seconds in milliseconds
        
        // Start vibration
        startVibration(vibrationDuration)
        
        // Create a full screen intent for high priority notifications
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            getNotificationId(seedName) + 1000, // Different ID to avoid conflicts
            intent,
            pendingIntentFlags
        )
        
        // Create a stop action intent
        val stopIntent = Intent(context, NotificationStopReceiver::class.java).apply {
            action = ACTION_STOP_NOTIFICATION
            putExtra("notification_id", getNotificationId(seedName))
        }
        
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            getNotificationId(seedName) + 2000, // Different ID to avoid conflicts
            stopIntent,
            pendingIntentFlags
        )
        
        // Create the notification
        val notification = NotificationCompat.Builder(context, "seed_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$seedName Available!")
            .setContentText("$seedName is now available with quantity: $quantity")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(false)
            // Do not set sound or vibration in notification (we handle it manually)
            .setVibrate(null) // No vibration pattern
            .setDefaults(0) // No defaults
            .setSound(null) // No sound
            .setOngoing(true) // Make it persistent until explicitly dismissed
            // Add stop action
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            // Ensure notification pops up on screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        // Add flags to ensure alert is shown
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        // Send the notification
        notificationManager.notify(getNotificationId(seedName), notification)
        Log.d("NotificationHelper", "Notification created for seed: $seedName with ID: ${getNotificationId(seedName)}")
    }
    
    // Stop notification and its sound/vibration effects
    fun stopNotification(notificationId: Int) {
        Log.d("NotificationHelper", "Stopping notification: $notificationId")
        
        try {
            // Stop the MediaPlayer
            synchronized(Companion) {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                    Log.d("NotificationHelper", "MediaPlayer stopped and released")
                }
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error stopping MediaPlayer: ${e.message}")
        }
        
        try {
            // Stop all system sounds
            RingtoneManager.getRingtone(context, 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.apply {
                if (isPlaying) {
                    stop()
                    Log.d("NotificationHelper", "System ringtone stopped")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error stopping system sounds: ${e.message}")
        }
        
        try {
            // Stop vibration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.cancel()
                Log.d("NotificationHelper", "Vibration canceled (Android 12+)")
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.cancel()
                Log.d("NotificationHelper", "Vibration canceled")
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error stopping vibration: ${e.message}")
        }
        
        // Remove the notification
        notificationManager.cancel(notificationId)
        Log.d("NotificationHelper", "Notification canceled: $notificationId")
    }
    
    // Stop all notifications and their effects
    fun stopAllNotifications() {
        Log.d("NotificationHelper", "Stopping all notifications")
        
        try {
            // Stop the MediaPlayer
            synchronized(Companion) {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                    Log.d("NotificationHelper", "MediaPlayer stopped and released")
                }
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error stopping MediaPlayer: ${e.message}")
        }
        
        try {
            // Stop vibration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.cancel()
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.cancel()
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error stopping vibration: ${e.message}")
        }
        
        // Don't cancel all notifications, as we might want to keep some
        // Just ensure no sound or vibration is active
    }
    
    // Play notification sound using MediaPlayer
    private fun playNotificationSound(soundUri: Uri) {
        try {
            // Ensure thread safety when dealing with the shared MediaPlayer
            synchronized(Companion) {
                // Release any previous player
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                mediaPlayer = null
                
                // Create and prepare new player
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, soundUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    
                    // Wake lock to ensure it plays when screen is off
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                    
                    isLooping = false
                    prepare()
                    start()
                    Log.d("NotificationHelper", "MediaPlayer started playing sound")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error playing notification sound: ${e.message}")
            // Fallback to default sound if custom sound fails
            try {
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, defaultSoundUri)
                ringtone.play()
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Error playing fallback sound: ${e.message}")
            }
        }
    }
    
    // Start vibration for the specified duration
    private fun startVibration(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
                Log.d("NotificationHelper", "Vibration started (Android 12+)")
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
                Log.d("NotificationHelper", "Vibration started")
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error starting vibration: ${e.message}")
        }
    }
    
    // Generate a unique notification ID for each gear
    private fun getNotificationId(gearName: String): Int {
        return gearName.hashCode()
    }
} 