package com.dainsleif.gaggy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationSettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsLayout: LinearLayout
    
    // List of gears from the image
    private val gears = listOf(
        "Watering Can",
        "Trowel",
        "Favorite Tool",
        "Basic Sprinkler",
        "Godly Sprinkler",
        "Advanced Sprinkler",
        "Master Sprinkler",
        "Lightning Rod",
        "Recall Wrench"
    )
    
    // List of seeds from the image
    private val seeds = listOf(
        "Carrot",
        "Strawberry",
        "Blueberry",
        "Orange Tulip",
        "Tomato",
        "Bamboo",
        "Watermelon",
        "Apple",
        "Pepper",
        "Mango",
        "Daffodil",
        "Pumpkin",
        "Corn",
        "Coconut",
        "Cactus",
        "Cacao",
        "Dragon Fruit",
        "Grape",
        "Mushroom",
        "Beanstalk"
    )
    
    // List of eggs from the image
    private val eggs = listOf(
        "Common Egg",
        "Rare Egg",
        "Uncommon Egg",
        "Legendary Egg",
        "Bug Egg",
        "Mythical Egg"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Set up back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        
        // Initialize the notification channels
        createGearNotificationChannel()
        createSeedNotificationChannel()
        createEggNotificationChannel()
        
        // Initialize UI components
        settingsLayout = findViewById(R.id.settingsLayout)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("Notifications", Context.MODE_PRIVATE)
        
        // Setup the UI for gear notification settings
        setupGearNotificationSettings()
        
        // Setup the UI for seed notification settings
        setupSeedNotificationSettings()
        
        // Setup the UI for egg notification settings
        setupEggNotificationSettings()
        
        // Add button to open battery optimization settings
        addBatteryOptimizationButton()
        
        // Listen for gear stock changes
        listenForGearStockChanges()
        
        // Listen for seed stock changes
        listenForSeedStockChanges()
        
        // Listen for egg stock changes
        listenForEggStockChanges()
    }
    
    private fun createGearNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Gear Notifications"
            val description = "Notifications for available gears"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("gear_channel", name, importance).apply {
                this.description = description
                
                // Enable lights and make it show as alert on lockscreen
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                
                // Enable vibration
                enableVibration(false)  // Disable channel vibration, we'll handle it manually
                
                // IMPORTANT: Disable sound on channel to prevent double sounds
                setSound(null, null)
                
                // Allow this channel to bypass Do Not Disturb mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setAllowBubbles(true)
                }
                
                // Show badge
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createSeedNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Seed Notifications"
            val description = "Notifications for available seeds"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("seed_channel", name, importance).apply {
                this.description = description
                
                // Enable lights and make it show as alert on lockscreen
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                
                // Enable vibration
                enableVibration(false)  // Disable channel vibration, we'll handle it manually
                
                // IMPORTANT: Disable sound on channel to prevent double sounds
                setSound(null, null)
                
                // Allow this channel to bypass Do Not Disturb mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setAllowBubbles(true)
                }
                
                // Show badge
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createEggNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Egg Notifications"
            val description = "Notifications for available eggs"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("egg_channel", name, importance).apply {
                this.description = description
                
                // Enable lights and make it show as alert on lockscreen
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                
                // Enable vibration
                enableVibration(false)  // Disable channel vibration, we'll handle it manually
                
                // IMPORTANT: Disable sound on channel to prevent double sounds
                setSound(null, null)
                
                // Allow this channel to bypass Do Not Disturb mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setAllowBubbles(true)
                }
                
                // Show badge
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun addBatteryOptimizationButton() {
        val batteryButton = Button(this)
        batteryButton.text = "Allow Battery Optimization Bypass"
        batteryButton.setPadding(16, 16, 16, 16)
        batteryButton.setOnClickListener {
            // Open battery optimization settings
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "Please find 'GAGgy' in the list and select 'Don't optimize'",
                Toast.LENGTH_LONG
            ).show()
        }
        
        // Add to layout
        val buttonLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonLayoutParams.setMargins(0, 32, 0, 32)
        batteryButton.layoutParams = buttonLayoutParams
        
        settingsLayout.addView(batteryButton)
    }
    
    private fun setupGearNotificationSettings() {
        // Add header
        val header = TextView(this)
        header.text = "Gear Notification Settings"
        header.textSize = 20f
        header.setPadding(0, 16, 0, 16)
        settingsLayout.addView(header)
        
        // Add a switch for each gear
        gears.forEachIndexed { index, gearName ->
            val switchLayout = LinearLayout(this)
            switchLayout.orientation = LinearLayout.HORIZONTAL
            switchLayout.setPadding(16, 16, 16, 16)
            
            val gearText = TextView(this)
            gearText.text = gearName
            gearText.textSize = 16f
            gearText.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            
            val switch = Switch(this)
            switch.isChecked = sharedPreferences.getBoolean(gearName, false)
            switch.tag = gearName
            switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                sharedPreferences.edit().putBoolean(gearName, isChecked).apply()
                Toast.makeText(
                    this,
                    "$gearName notifications ${if (isChecked) "enabled" else "disabled"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            switchLayout.addView(gearText)
            switchLayout.addView(switch)
            
            settingsLayout.addView(switchLayout)
            
            // Add a divider except for the last item
            if (index < gears.size - 1) {
                val divider = View(this)
                divider.setBackgroundColor(getColor(android.R.color.darker_gray))
                val dividerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                divider.layoutParams = dividerParams
                settingsLayout.addView(divider)
            }
        }
    }
    
    private fun setupSeedNotificationSettings() {
        // Add header
        val header = TextView(this)
        header.text = "Seed Notification Settings"
        header.textSize = 20f
        header.setPadding(0, 16, 0, 16)
        settingsLayout.addView(header)
        
        // Add a switch for each seed
        seeds.forEachIndexed { index, seedName ->
            val switchLayout = LinearLayout(this)
            switchLayout.orientation = LinearLayout.HORIZONTAL
            switchLayout.setPadding(16, 16, 16, 16)
            
            val seedText = TextView(this)
            seedText.text = seedName
            seedText.textSize = 16f
            seedText.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            
            val switch = Switch(this)
            switch.isChecked = sharedPreferences.getBoolean(seedName, false)
            switch.tag = seedName
            switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                sharedPreferences.edit().putBoolean(seedName, isChecked).apply()
                Toast.makeText(
                    this,
                    "$seedName notifications ${if (isChecked) "enabled" else "disabled"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            switchLayout.addView(seedText)
            switchLayout.addView(switch)
            
            settingsLayout.addView(switchLayout)
            
            // Add a divider except for the last item
            if (index < seeds.size - 1) {
                val divider = View(this)
                divider.setBackgroundColor(getColor(android.R.color.darker_gray))
                val dividerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                divider.layoutParams = dividerParams
                settingsLayout.addView(divider)
            }
        }
    }
    
    private fun setupEggNotificationSettings() {
        // Add header
        val header = TextView(this)
        header.text = "Egg Notification Settings"
        header.textSize = 20f
        header.setPadding(0, 16, 0, 16)
        settingsLayout.addView(header)
        
        // Add a switch for each egg
        eggs.forEachIndexed { index, eggName ->
            val switchLayout = LinearLayout(this)
            switchLayout.orientation = LinearLayout.HORIZONTAL
            switchLayout.setPadding(16, 16, 16, 16)
            
            val eggText = TextView(this)
            eggText.text = eggName
            eggText.textSize = 16f
            eggText.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            
            val switch = Switch(this)
            switch.isChecked = sharedPreferences.getBoolean(eggName, false)
            switch.tag = eggName
            switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                sharedPreferences.edit().putBoolean(eggName, isChecked).apply()
                Toast.makeText(
                    this,
                    "$eggName notifications ${if (isChecked) "enabled" else "disabled"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            switchLayout.addView(eggText)
            switchLayout.addView(switch)
            
            settingsLayout.addView(switchLayout)
            
            // Add a divider except for the last item
            if (index < eggs.size - 1) {
                val divider = View(this)
                divider.setBackgroundColor(getColor(android.R.color.darker_gray))
                val dividerParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                divider.layoutParams = dividerParams
                settingsLayout.addView(divider)
            }
        }
    }
    
    private fun listenForGearStockChanges() {
        val database = FirebaseDatabase.getInstance().reference.child("discord_data")
        database.child("stocks").child("GEAR STOCK").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { gearSnapshot ->
                    val name = gearSnapshot.child("name").getValue(String::class.java) ?: ""
                    val quantity = gearSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    
                    // If quantity is greater than 0 and notifications are enabled for this gear, send notification
                    if (quantity > 0 && sharedPreferences.getBoolean(name, false)) {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notificationHelper = NotificationHelper.getInstance(this@NotificationSettingsActivity)
                        notificationHelper.createGearNotification(name, quantity)
                        
                        // Mark as seen (you could implement additional logic here for "seen" status)
                        sharedPreferences.edit().putBoolean("${name}_seen", true).apply()
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    
    private fun listenForSeedStockChanges() {
        val database = FirebaseDatabase.getInstance().reference.child("discord_data")
        Log.d("SeedNotification", "Setting up seed stock listener")
        database.child("stocks").child("SEEDS STOCK").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("SeedNotification", "Received seed stock data: ${snapshot.childrenCount} items")
                snapshot.children.forEach { seedSnapshot ->
                    val name = seedSnapshot.child("name").getValue(String::class.java) ?: ""
                    val quantity = seedSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    
                    Log.d("SeedNotification", "Seed: $name, Quantity: $quantity, Notification enabled: ${sharedPreferences.getBoolean(name, false)}")
                    
                    // If quantity is greater than 0 and notifications are enabled for this seed, send notification
                    if (quantity > 0 && sharedPreferences.getBoolean(name, false)) {
                        Log.d("SeedNotification", "Triggering notification for seed: $name")
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notificationHelper = NotificationHelper.getInstance(this@NotificationSettingsActivity)
                        notificationHelper.createSeedNotification(name, quantity)
                        
                        // Mark as seen (you could implement additional logic here for "seen" status)
                        sharedPreferences.edit().putBoolean("${name}_seen", true).apply()
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("SeedNotification", "Firebase error: ${error.message}")
                // Handle error
            }
        })
    }
    
    private fun listenForEggStockChanges() {
        val database = FirebaseDatabase.getInstance().reference.child("discord_data")
        Log.d("EggNotification", "Setting up egg stock listener")
        database.child("eggs").child("EGG STOCK").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("EggNotification", "Received egg stock data: ${snapshot.childrenCount} items")
                snapshot.children.forEach { eggSnapshot ->
                    val name = eggSnapshot.child("name").getValue(String::class.java) ?: ""
                    val quantity = eggSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    
                    Log.d("EggNotification", "Egg: $name, Quantity: $quantity, Notification enabled: ${sharedPreferences.getBoolean(name, false)}")
                    
                    // If quantity is greater than 0 and notifications are enabled for this egg, send notification
                    if (quantity > 0 && sharedPreferences.getBoolean(name, false)) {
                        Log.d("EggNotification", "Triggering notification for egg: $name")
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notificationHelper = NotificationHelper.getInstance(this@NotificationSettingsActivity)
                        notificationHelper.createEggNotification(name, quantity)
                        
                        // Mark as seen (you could implement additional logic here for "seen" status)
                        sharedPreferences.edit().putBoolean("${name}_seen", true).apply()
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("EggNotification", "Firebase error: ${error.message}")
                // Handle error
            }
        })
    }
} 