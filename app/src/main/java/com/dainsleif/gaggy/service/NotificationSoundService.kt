package com.dainsleif.gaggy.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dainsleif.gaggy.MainActivity
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.model.GardenData
import com.dainsleif.gaggy.model.ItemData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationSoundService(private val context: Context) {

    private val PREFS_NAME = "GardenEggPrefs"
    private val PREF_PREFIX_EGG = "egg_"
    private val PREF_PREFIX_SETTING = "setting_"
    
    private val NOTIFICATION_CHANNEL_ID = "garden_eggs_channel"
    private val NOTIFICATION_ID = 1001
    
    // Duration for continuous vibration (3000ms = 3 seconds)
    private val VIBRATION_DURATION = 3000L
    
    private var mediaPlayer: MediaPlayer? = null
    private var previousEggData: Map<String, List<ItemData>> = mapOf()
    private var firebaseListener: ValueEventListener? = null
    
    init {
        createNotificationChannel()
        setupFirebaseListener()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Garden Eggs"
            val descriptionText = "Notifications for new garden eggs"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                // No vibration pattern for continuous vibration
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun setupFirebaseListener() {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("/")
        
        firebaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val gardenData = snapshot.getValue(GardenData::class.java)
                    gardenData?.let {
                        processGardenData(it)
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        
        reference.addValueEventListener(firebaseListener!!)
    }
    
    private fun processGardenData(gardenData: GardenData) {
        // Check if all notifications are enabled
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val allNotificationsEnabled = sharedPrefs.getBoolean("${PREF_PREFIX_SETTING}all_notifications", true)
        val soundEnabled = sharedPrefs.getBoolean("${PREF_PREFIX_SETTING}sound", true)
        val vibrationEnabled = sharedPrefs.getBoolean("${PREF_PREFIX_SETTING}vibration", true)
        
        if (!allNotificationsEnabled) {
            return
        }
        
        // Process eggs
        val currentEggs = mutableMapOf<String, List<ItemData>>()
        val newEggs = mutableListOf<String>()
        
        // Add eggs
        gardenData.datas.eggs?.let { eggData ->
            currentEggs["Common Egg"] = eggData.items.filter { it.name == "Common Egg" }
            currentEggs["Rare Egg"] = eggData.items.filter { it.name == "Rare Egg" }
            currentEggs["Uncommon Egg"] = eggData.items.filter { it.name == "Uncommon Egg" }
            currentEggs["Legendary Egg"] = eggData.items.filter { it.name == "Legendary Egg" }
            currentEggs["Bug Egg"] = eggData.items.filter { it.name == "Bug Egg" }
            currentEggs["Mythical Egg"] = eggData.items.filter { it.name == "Mythical Egg" }
            currentEggs["Paradise Egg"] = eggData.items.filter { it.name == "Paradise Egg" }
            currentEggs["Common Summer Egg"] = eggData.items.filter { it.name == "Common Summer Egg" }
            currentEggs["Rare Summer Egg"] = eggData.items.filter { it.name == "Rare Summer Egg" }
        }
        
        // Check if this is the first data load
        if (previousEggData.isEmpty()) {
            previousEggData = currentEggs
            return
        }
        
        // Check for changes in egg quantities
        var shouldPlaySound = false
        var shouldVibrate = false
        
        for ((eggName, items) in currentEggs) {
            val prefKey = PREF_PREFIX_EGG + eggName.replace(" ", "_").lowercase()
            val isEggEnabled = sharedPrefs.getBoolean(prefKey, false)
            
            if (isEggEnabled) {
                val previousItems = previousEggData[eggName] ?: emptyList()
                val previousCount = previousItems.sumOf { it.quantity }
                val currentCount = items.sumOf { it.quantity }
                
                if (currentCount > previousCount) {
                    // We have new eggs of this type
                    shouldPlaySound = true
                    shouldVibrate = true
                    val newCount = currentCount - previousCount
                    newEggs.add("$newCount $eggName")
                }
            }
        }
        
        // Update previous data
        previousEggData = currentEggs
        
        // Show notification and play sound if needed
        if (newEggs.isNotEmpty()) {
            showNotification(newEggs, vibrationEnabled)
            
            if (shouldPlaySound && soundEnabled) {
                playUrgentSound()
            }
            
            if (shouldVibrate && vibrationEnabled) {
                vibrate()
            }
        }
    }
    
    private fun showNotification(newEggs: List<String>, vibrationEnabled: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = if (newEggs.size == 1) {
            "New ${newEggs.first()} available!"
        } else {
            "New eggs available: ${newEggs.joinToString(", ")}"
        }
        
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Garden Eggs Update")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // We don't set vibration on the notification anymore since we're handling it separately
        
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }
    
    private fun playUrgentSound() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Release any existing MediaPlayer
                mediaPlayer?.release()
                
                // Create new MediaPlayer
                mediaPlayer = MediaPlayer.create(context, R.raw.urgent)
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                mediaPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                
                // Use createOneShot for continuous vibration
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Use createOneShot for continuous vibration
                    vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(VIBRATION_DURATION)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun release() {
        // Remove Firebase listener
        firebaseListener?.let {
            val database = FirebaseDatabase.getInstance()
            val reference = database.getReference("/")
            reference.removeEventListener(it)
        }
        
        // Release MediaPlayer
        mediaPlayer?.release()
        mediaPlayer = null
    }
} 