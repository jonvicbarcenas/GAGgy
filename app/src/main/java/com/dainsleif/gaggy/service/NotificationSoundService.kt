package com.dainsleif.gaggy.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import java.util.Locale
import java.util.UUID

class NotificationSoundService(private val context: Context) {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "garden_notification_channel"
        const val ACTION_STOP_SOUND = "com.dainsleif.gaggy.STOP_SOUND"
    }
    
    private val PREFS_NAME = "GardenEggPrefs"
    private val PREF_PREFIX_EGG = "egg_"
    private val PREF_PREFIX_GEAR = "gear_"
    private val PREF_PREFIX_SETTING = "setting_"
    private val PREF_PREFIX_SEED = "seed_"
    
    private val NOTIFICATION_ID = 1001
    
    // Duration for continuous vibration (3000ms = 3 seconds)
    private val VIBRATION_DURATION = 3000L
    
    private var mediaPlayer: MediaPlayer? = null
    private var previousEggData: Map<String, List<ItemData>> = mapOf()
    private var previousGearData: Map<String, List<ItemData>> = mapOf()
    private var previousSeedData: Map<String, List<ItemData>> = mapOf()
    private var firebaseListener: ValueEventListener? = null
    
    // Text to speech
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    
    // Action for stop button
    private val stopSoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STOP_SOUND) {
                stopSoundAndVibration()
            }
        }
    }
    
    init {
        createNotificationChannel()
        setupFirebaseListener()
           // Register the broadcast receiver with the appropriate flag for Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(stopSoundReceiver, IntentFilter(ACTION_STOP_SOUND), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(stopSoundReceiver, IntentFilter(ACTION_STOP_SOUND))
        }
        // Initialize Text-to-Speech
        initTextToSpeech()
    }
    
    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Language not supported
                } else {
                    isTtsReady = true
                }
            }
        }
        
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            
            override fun onDone(utteranceId: String?) {}
            
            override fun onError(utteranceId: String?) {}
        })
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
        // Shared preferences for notification toggles
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val soundEnabled = sharedPrefs.getBoolean("${PREF_PREFIX_SETTING}sound", true)
        val vibrationEnabled = sharedPrefs.getBoolean("${PREF_PREFIX_SETTING}vibration", true)
        val ttsEnabled = sharedPrefs.getBoolean("${PREF_PREFIX_SETTING}text_to_speech", true)
        
        // Lists to store new items
        val newEggs = mutableListOf<String>()
        val newGear = mutableListOf<String>()
        val newSeeds = mutableListOf<String>()
        
        // Process eggs data
        val currentEggs = mutableMapOf<String, List<ItemData>>()
        
        // Add eggs
        gardenData.datas.eggs?.let { eggData ->
            currentEggs["Common Egg"] = eggData.items.filter { it.name == "Common Egg" }
            currentEggs["Rare Egg"] = eggData.items.filter { it.name == "Rare Egg" }
            currentEggs["Uncommon Egg"] = eggData.items.filter { it.name == "Uncommon Egg" }
            currentEggs["Legendary Egg"] = eggData.items.filter { it.name == "Legendary Egg" }
            currentEggs["Bug Egg"] = eggData.items.filter { it.name == "Bug Egg" }
            currentEggs["Bee Egg"] = eggData.items.filter { it.name == "Bee Egg" }
            currentEggs["Mythical Egg"] = eggData.items.filter { it.name == "Mythical Egg" }
            currentEggs["Paradise Egg"] = eggData.items.filter { it.name == "Paradise Egg" }
            currentEggs["Common Summer Egg"] = eggData.items.filter { it.name == "Common Summer Egg" }
            currentEggs["Rare Summer Egg"] = eggData.items.filter { it.name == "Rare Summer Egg" }
        }
        
        // Process gear data
        val currentGear = mutableMapOf<String, List<ItemData>>()
        
        // Add gear items if available
        gardenData.datas.stocks?.gear?.let { gearData ->
            currentGear["Watering Can"] = gearData.items.filter { it.name == "Watering Can" }
            currentGear["Levelup Lollipop"] = gearData.items.filter { it.name == "Levelup Lollipop" }
            currentGear["Medium Toy"] = gearData.items.filter { it.name == "Medium Toy" }
            currentGear["Medium Treat"] = gearData.items.filter { it.name == "Medium Treat" }
            currentGear["Trowel"] = gearData.items.filter { it.name == "Trowel" }
            currentGear["Favorite Tool"] = gearData.items.filter { it.name == "Favorite Tool" }
            currentGear["Basic Sprinkler"] = gearData.items.filter { it.name == "Basic Sprinkler" }
            currentGear["Godly Sprinkler"] = gearData.items.filter { it.name == "Godly Sprinkler" }
            currentGear["Advanced Sprinkler"] = gearData.items.filter { it.name == "Advanced Sprinkler" }
            currentGear["Master Sprinkler"] = gearData.items.filter { it.name == "Master Sprinkler" }
            currentGear["Magnifying Glass"] = gearData.items.filter { it.name == "Magnifying Glass" }
            currentGear["Recall Wrench"] = gearData.items.filter { it.name == "Recall Wrench" }
            currentGear["Harvest Tool"] = gearData.items.filter { it.name == "Harvest Tool" }
            currentGear["Friendship Pot"] = gearData.items.filter { it.name == "Friendship Pot" }
            currentGear["Cleaning Spray"] = gearData.items.filter { it.name == "Cleaning Spray" }
            currentGear["Tanning Mirror"] = gearData.items.filter { it.name == "Tanning Mirror" }
        }
        
        // Process seeds data
        val currentSeeds = mutableMapOf<String, List<ItemData>>()
        
        // Add seed items if available
        gardenData.datas.stocks?.seeds?.let { seedsData ->
            currentSeeds["Watermelon"] = seedsData.items.filter { it.name == "Watermelon" }
            currentSeeds["Pumpkin"] = seedsData.items.filter { it.name == "Pumpkin" }
            currentSeeds["Coconut"] = seedsData.items.filter { it.name == "Coconut" }
            currentSeeds["Cactus"] = seedsData.items.filter { it.name == "Cactus" }
            currentSeeds["Dragon Fruit"] = seedsData.items.filter { it.name == "Dragon Fruit" }
            currentSeeds["Mango"] = seedsData.items.filter { it.name == "Mango" }
            currentSeeds["Grape"] = seedsData.items.filter { it.name == "Grape" }
            currentSeeds["Mushroom"] = seedsData.items.filter { it.name == "Mushroom" }
            currentSeeds["Pepper"] = seedsData.items.filter { it.name == "Pepper" }
            currentSeeds["Cacao"] = seedsData.items.filter { it.name == "Cacao" }
            currentSeeds["Beanstalk"] = seedsData.items.filter { it.name == "Beanstalk" }
            currentSeeds["Ember Lily"] = seedsData.items.filter { it.name == "Ember Lily" }
            currentSeeds["Sugar Apple"] = seedsData.items.filter { it.name == "Sugar Apple" }
            currentSeeds["Burning Bud"] = seedsData.items.filter { it.name == "Burning Bud" }
            currentSeeds["Giant Pinecone"] = seedsData.items.filter { it.name == "Giant Pinecone" }
        }
        
        // Check if this is the first data load for eggs
        if (previousEggData.isEmpty()) {
            previousEggData = currentEggs
            previousGearData = currentGear
            previousSeedData = currentSeeds
        } else {
            // Check for changes in egg quantities
            for ((eggName, items) in currentEggs) {
                val prefKey = PREF_PREFIX_EGG + eggName.replace(" ", "_").lowercase()
                val isEggEnabled = sharedPrefs.getBoolean(prefKey, false)
                
                if (isEggEnabled) {
                    val previousItems = previousEggData[eggName] ?: emptyList()
                    val previousCount = previousItems.sumOf { it.quantity }
                    val currentCount = items.sumOf { it.quantity }
                    
                    if (currentCount > previousCount) {
                        // We have new eggs of this type
                        val newCount = currentCount - previousCount
                        newEggs.add("$newCount $eggName")
                    }
                }
            }
            
            // Update previous egg data
            previousEggData = currentEggs
            
            // Check for changes in gear quantities
            for ((gearName, items) in currentGear) {
                val prefKey = PREF_PREFIX_GEAR + gearName.replace(" ", "_").lowercase()
                val isGearEnabled = sharedPrefs.getBoolean(prefKey, false)
                
                if (isGearEnabled) {
                    val previousItems = previousGearData[gearName] ?: emptyList()
                    val previousCount = previousItems.sumOf { it.quantity }
                    val currentCount = items.sumOf { it.quantity }
                    
                    if (currentCount > previousCount) {
                        // We have new gear of this type
                        val newCount = currentCount - previousCount
                        newGear.add("$newCount $gearName")
                    }
                }
            }
            
            // Update previous gear data
            previousGearData = currentGear
            
            // Check for changes in seeds quantities
            for ((seedName, items) in currentSeeds) {
                val prefKey = PREF_PREFIX_SEED + seedName.replace(" ", "_").lowercase()
                val isSeedItemEnabled = sharedPrefs.getBoolean(prefKey, false)
                
                if (isSeedItemEnabled) {
                    val previousItems = previousSeedData[seedName] ?: emptyList()
                    val previousCount = previousItems.sumOf { it.quantity }
                    val currentCount = items.sumOf { it.quantity }
                    
                    if (currentCount > previousCount) {
                        // We have new seed items of this type
                        val newCount = currentCount - previousCount
                        newSeeds.add("$newCount $seedName")
                    }
                }
            }
            
            // Update previous seed data
            previousSeedData = currentSeeds
        }

        
        // Show notification for eggs if needed
        if (newEggs.isNotEmpty()) {
            showNotification(newEggs, "New Eggs Available", vibrationEnabled)
            
            if (soundEnabled || vibrationEnabled) {
                playUrgentSound()
                vibrate()
            }
            
            if (ttsEnabled) {
                speakNotification("New Eggs Available", newEggs)
            }
        }
        
        // Show notification for seeds if needed
        if (newSeeds.isNotEmpty()) {
            showNotification(newSeeds, "New Seeds Available", vibrationEnabled)
            
            if (soundEnabled || vibrationEnabled) {
                playUrgentSound()
                vibrate()
            }
            
            if (ttsEnabled) {
                speakNotification("New Seeds Available", newSeeds)
            }
        }
        
        // Show notification for gear if needed
        if (newGear.isNotEmpty()) {
            showNotification(newGear, "New Gear Available", vibrationEnabled)

            playUrgentSound()
            vibrate()
            
            if (ttsEnabled) {
                speakNotification("New Gear Available", newGear)
            }
        }
    }
    
    private fun showNotification(items: List<String>, title: String, vibrationEnabled: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create stop sound intent
        val stopIntent = Intent(ACTION_STOP_SOUND)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentText = if (items.size == 1) {
            "New ${items.first()} available!"
        } else {
            "New items available: ${items.joinToString(", ")}"
        }
        
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop Sound", stopPendingIntent)
        
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
    
    private fun speakNotification(title: String, items: List<String>) {
        if (!isTtsReady || textToSpeech == null) return
        
        val textToSpeak = if (items.size == 1) {
            "$title. New ${items.first()} available!"
        } else {
            "$title. New items available: ${items.joinToString(", ")}"
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        } else {
            @Suppress("DEPRECATION")
            textToSpeech?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null)
        }
    }
    
    private fun stopSoundAndVibration() {
        // Stop media player
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
            mediaPlayer = null
        }
        
        // Stop vibration
        try {
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
            e.printStackTrace()
        }
        
        // Stop TTS
        textToSpeech?.stop()
        
        // Clear the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)
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
        
        // Shutdown TTS
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        
        // Unregister broadcast receiver
        try {
            context.unregisterReceiver(stopSoundReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
} 