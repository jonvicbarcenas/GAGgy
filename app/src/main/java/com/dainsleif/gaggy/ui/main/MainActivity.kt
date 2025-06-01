package com.dainsleif.gaggy.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.notifications.NotificationChannelManager
import com.dainsleif.gaggy.notifications.PermissionHandler
import com.dainsleif.gaggy.ui.settings.NotificationSettingsActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var seedsLayout: LinearLayout
    private lateinit var gearLayout: LinearLayout
    private lateinit var eggsLayout: LinearLayout
    private lateinit var honeyLayout: LinearLayout
    private lateinit var lastUpdatedTextView: TextView
    private lateinit var notificationsButton: Button
    private lateinit var viewModel: MainViewModel
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var notificationChannelManager: NotificationChannelManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Initialize permission handler
        permissionHandler = PermissionHandler(this)
        
        // Initialize notification channels
        notificationChannelManager = NotificationChannelManager(this)
        notificationChannelManager.createAllChannels()
        
        // Request permissions
        requestPermissions()
        
        // Initialize UI components
        initializeUI()
        
        // Observe ViewModel data
        observeViewModel()
        
        // Listen for last updated times
        listenForLastUpdatedTimes()
    }
    
    private fun initializeUI() {
        seedsLayout = findViewById(R.id.seedsLayout)
        gearLayout = findViewById(R.id.gearLayout)
        eggsLayout = findViewById(R.id.eggsLayout)
        honeyLayout = findViewById(R.id.honeyLayout)
        lastUpdatedTextView = findViewById(R.id.lastUpdatedTextView)
        notificationsButton = findViewById(R.id.notificationsButton)
        
        // Set up notification settings button
        notificationsButton.setOnClickListener {
            val intent = Intent(this, NotificationSettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun observeViewModel() {
        // Observe gear items
        viewModel.gearItems.observe(this) { gearItems ->
            updateGearUI(gearItems)
        }
        
        // Observe seed items
        viewModel.seedItems.observe(this) { seedItems ->
            updateSeedUI(seedItems)
        }
        
        // Observe egg items
        viewModel.eggItems.observe(this) { eggItems ->
            updateEggUI(eggItems)
        }
        
        // Observe honey items
        viewModel.honeyItems.observe(this) { honeyItems ->
            updateHoneyUI(honeyItems)
        }
        
        // Observe last updated times
        viewModel.lastUpdated.observe(this) { (stocksTime, eggsTime, honeyTime) ->
            lastUpdatedTextView.text = "Last Updated - Stocks: $stocksTime | Eggs: $eggsTime | Honey: $honeyTime"
        }
    }
    
    private fun updateGearUI(items: List<Item>) {
        gearLayout.removeAllViews()
        addSectionHeader(gearLayout, "GEAR STOCK")
        items.forEach { item ->
            addItemToSection(gearLayout, "${item.name}: ${item.quantity}")
        }
    }
    
    private fun updateSeedUI(items: List<Item>) {
        seedsLayout.removeAllViews()
        addSectionHeader(seedsLayout, "SEEDS STOCK")
        items.forEach { item ->
            addItemToSection(seedsLayout, "${item.name}: ${item.quantity}")
        }
    }
    
    private fun updateEggUI(items: List<Item>) {
        eggsLayout.removeAllViews()
        addSectionHeader(eggsLayout, "EGG STOCK")
        items.forEach { item ->
            addItemToSection(eggsLayout, "${item.name}: ${item.quantity}")
        }
    }
    
    private fun updateHoneyUI(items: List<Item>) {
        honeyLayout.removeAllViews()
        addSectionHeader(honeyLayout, "HONEY STOCK")
        items.forEach { item ->
            addItemToSection(honeyLayout, "${item.name}: ${item.quantity}")
        }
    }
    
    private fun requestPermissions() {
        // Request notification permission if not granted
        if (!permissionHandler.hasNotificationPermission()) {
            permissionHandler.showNotificationPermissionDialog(this)
        }
        
        // Request battery optimization exemption
        permissionHandler.requestBatteryOptimizationExemption(this)
    }
    
    private fun listenForLastUpdatedTimes() {
        val database = FirebaseDatabase.getInstance().reference.child("discord_data").child("last_updated")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stocksTime = snapshot.child("stocks").child("ph").getValue(String::class.java) ?: ""
                val eggsTime = snapshot.child("eggs").child("ph").getValue(String::class.java) ?: ""
                val honeyTime = snapshot.child("honeyStocks").child("ph").getValue(String::class.java) ?: ""
                viewModel.updateLastUpdated(stocksTime, eggsTime, honeyTime)
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PermissionHandler.PERMISSION_REQUEST_NOTIFICATION) {
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