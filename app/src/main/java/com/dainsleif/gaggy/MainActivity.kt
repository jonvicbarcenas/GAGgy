package com.dainsleif.gaggy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var seedsLayout: LinearLayout
    private lateinit var gearLayout: LinearLayout
    private lateinit var eggsLayout: LinearLayout
    private lateinit var lastUpdatedTextView: TextView
    private lateinit var notificationsButton: Button
    private lateinit var notificationHelper: NotificationHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize notification helper
        notificationHelper = NotificationHelper.getInstance(this)
        
        // Create notification channels
        createNotificationChannels()
        
        // Request permissions
        requestBatteryOptimizationExemption()
        requestNotificationPermission()
        
        // Initialize UI components
        seedsLayout = findViewById(R.id.seedsLayout)
        gearLayout = findViewById(R.id.gearLayout)
        eggsLayout = findViewById(R.id.eggsLayout)
        lastUpdatedTextView = findViewById(R.id.lastUpdatedTextView)
        notificationsButton = findViewById(R.id.notificationsButton)
        
        // Set up notification settings button
        notificationsButton.setOnClickListener {
            val intent = Intent(this, NotificationSettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference.child("discord_data")
        
        // Set up real-time listener
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateUI(snapshot)
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            
            // Check if channels already exist
            val existingChannels = notificationManager.notificationChannels
            val hasGearChannel = existingChannels.any { it.id == "gear_channel" }
            val hasSeedChannel = existingChannels.any { it.id == "seed_channel" }
            
            // Only create channels if they don't exist
            if (!hasGearChannel || !hasSeedChannel) {
                // Notification settings activity will create them
                val intent = Intent(this, NotificationSettingsActivity::class.java)
                startActivity(intent)
                finish() // Close current activity to ensure channels are created first
                return
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (!notificationHelper.hasNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Show explanation dialog before requesting permission
                AlertDialog.Builder(this)
                    .setTitle("Notification Permission Required")
                    .setMessage("This app needs notification permission to alert you when gear or seeds become available. Without notifications, the app cannot function properly.")
                    .setPositiveButton("Enable Notifications") { _, _ ->
                        notificationHelper.requestNotificationPermission(this)
                    }
                    .setNegativeButton("Open Settings") { _, _ ->
                        // Open app notification settings directly
                        val intent = Intent().apply {
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        }
                        startActivity(intent)
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == NotificationHelper.PERMISSION_REQUEST_NOTIFICATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied. Alerts won't work properly.", Toast.LENGTH_LONG).show()
                
                // Show settings dialog to encourage user to enable permissions manually
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Notifications are required for this app to function properly. Would you like to enable them in settings?")
                    .setPositiveButton("Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    // Intent to directly request ignoring battery optimization
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general battery optimization settings if direct request fails
                    Toast.makeText(
                        this,
                        "Please disable battery optimization for this app to ensure notifications work properly",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun updateUI(snapshot: DataSnapshot) {
        // Clear previous data
        seedsLayout.removeAllViews()
        gearLayout.removeAllViews()
        eggsLayout.removeAllViews()
        
        // Update stocks section
        val stocks = snapshot.child("stocks")
        
        // Seeds stock
        val seedsStock = stocks.child("SEEDS STOCK")
        addSectionHeader(seedsLayout, "SEEDS STOCK")
        seedsStock.children.forEach { seedSnapshot ->
            val name = seedSnapshot.child("name").getValue(String::class.java) ?: ""
            val quantity = seedSnapshot.child("quantity").getValue(Int::class.java) ?: 0
            addItemToSection(seedsLayout, "$name: $quantity")
        }
        
        // Gear stock
        val gearStock = stocks.child("GEAR STOCK")
        addSectionHeader(gearLayout, "GEAR STOCK")
        gearStock.children.forEach { gearSnapshot ->
            val name = gearSnapshot.child("name").getValue(String::class.java) ?: ""
            val quantity = gearSnapshot.child("quantity").getValue(Int::class.java) ?: 0
            addItemToSection(gearLayout, "$name: $quantity")
        }
        
        // Eggs section
        val eggs = snapshot.child("eggs").child("EGG STOCK")
        addSectionHeader(eggsLayout, "EGG STOCK")
        eggs.children.forEach { eggSnapshot ->
            val name = eggSnapshot.child("name").getValue(String::class.java) ?: ""
            val quantity = eggSnapshot.child("quantity").getValue(Int::class.java) ?: 0
            addItemToSection(eggsLayout, "$name: $quantity")
        }
        
        // Last updated section
        val lastUpdated = snapshot.child("last_updated")
        val stocksTime = lastUpdated.child("stocks").child("ph").getValue(String::class.java) ?: ""
        val eggsTime = lastUpdated.child("eggs").child("ph").getValue(String::class.java) ?: ""
        lastUpdatedTextView.text = "Last Updated - Stocks: $stocksTime | Eggs: $eggsTime"
    }
    
    private fun addSectionHeader(layout: LinearLayout, title: String) {
        val header = TextView(this)
        header.text = title
        header.textSize = 18f
        header.setTypeface(null, android.graphics.Typeface.BOLD)
        header.setPadding(0, 16, 0, 8)
        layout.addView(header)
    }
    
    private fun addItemToSection(layout: LinearLayout, text: String) {
        val item = TextView(this)
        item.text = text
        item.textSize = 14f
        item.setPadding(16, 4, 0, 4)
        layout.addView(item)
    }
}