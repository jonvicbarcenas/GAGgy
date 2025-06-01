package com.dainsleif.gaggy.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Handles sound and vibration for notifications
 */
class MediaManager private constructor(private val context: Context) {
    companion object {
        private const val TAG = "MediaManager"
        
        // Static MediaPlayer instance shared across all instances
        private var mediaPlayer: MediaPlayer? = null
        
        // Singleton instance
        @Volatile
        private var INSTANCE: MediaManager? = null
        
        // Get singleton instance
        fun getInstance(context: Context): MediaManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MediaManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Plays notification sound using MediaPlayer
     */
    fun playNotificationSound(soundUri: Uri) {
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
                    Log.d(TAG, "MediaPlayer started playing sound")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing notification sound: ${e.message}")
            // Fallback to default sound if custom sound fails
            try {
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, defaultSoundUri)
                ringtone.play()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing fallback sound: ${e.message}")
            }
        }
    }
    
    /**
     * Start vibration for the specified duration
     */
    fun startVibration(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
                Log.d(TAG, "Vibration started (Android 12+)")
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
                Log.d(TAG, "Vibration started")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration: ${e.message}")
        }
    }
    
    /**
     * Stop all media playback and vibration
     */
    fun stopAllMedia() {
        stopMediaPlayer()
        stopVibration()
        stopSystemSounds()
    }
    
    private fun stopMediaPlayer() {
        try {
            // Stop the MediaPlayer
            synchronized(Companion) {
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                    Log.d(TAG, "MediaPlayer stopped and released")
                }
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaPlayer: ${e.message}")
        }
    }
    
    private fun stopVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.cancel()
                Log.d(TAG, "Vibration canceled (Android 12+)")
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.cancel()
                Log.d(TAG, "Vibration canceled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration: ${e.message}")
        }
    }
    
    private fun stopSystemSounds() {
        try {
            // Stop all system sounds
            RingtoneManager.getRingtone(context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))?.apply {
                if (isPlaying) {
                    stop()
                    Log.d(TAG, "System ringtone stopped")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping system sounds: ${e.message}")
        }
    }
} 