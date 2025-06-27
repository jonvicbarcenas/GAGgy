package com.dainsleif.gaggy.ui.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dainsleif.gaggy.GaggyApplication
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.data.models.Weather
import com.dainsleif.gaggy.notifications.NotificationChannelManager
import com.dainsleif.gaggy.notifications.PermissionHandler
import com.dainsleif.gaggy.ui.settings.NotificationSettingsActivity
import com.dainsleif.gaggy.util.AppUpdateChecker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var seedsLayout: LinearLayout
    private lateinit var gearLayout: LinearLayout
    private lateinit var eggsLayout: LinearLayout
    private lateinit var weatherTitleTextView: TextView
    private lateinit var weatherDescriptionTextView: TextView
    private lateinit var lastUpdatedTextView: TextView
    private lateinit var weatherLastUpdatedTextView: TextView
    private lateinit var notificationsButton: Button
    private lateinit var menuButton: ImageButton
    private lateinit var viewModel: MainViewModel
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var notificationChannelManager: NotificationChannelManager
    private lateinit var appUpdateChecker: AppUpdateChecker
    
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
        
        // Initialize app update checker
        appUpdateChecker = AppUpdateChecker(this)
        
        // Request permissions
        requestPermissions()
        
        // Request battery optimization exemption
        requestBatteryOptimizationExemption()
        
        // Request auto-start permission for specific manufacturers
        requestAutoStartPermission()
        
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
        weatherTitleTextView = findViewById(R.id.weatherTitleTextView)
        weatherDescriptionTextView = findViewById(R.id.weatherDescriptionTextView)
        lastUpdatedTextView = findViewById(R.id.lastUpdatedTextView)
        weatherLastUpdatedTextView = findViewById(R.id.weatherLastUpdatedTextView)
        notificationsButton = findViewById(R.id.notificationsButton)
        menuButton = findViewById(R.id.menuButton)
        
        // Set up notification settings button
        notificationsButton.setOnClickListener {
            val intent = Intent(this, NotificationSettingsActivity::class.java)
            startActivity(intent)
        }
        
        // Set up menu button
        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }
    
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_check_update -> {
                    checkForUpdates()
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
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
        
        // Observe weather
        viewModel.weather.observe(this) { weather ->
            updateWeatherUI(weather)
        }
        
        // Observe last updated times
        viewModel.lastUpdated.observe(this) { (stocksTime, eggsTime) ->
            lastUpdatedTextView.text = "Last Updated - Stocks: $stocksTime | Eggs: $eggsTime"
        }
        
        // Observe weather last updated time
        viewModel.weatherLastUpdated.observe(this) { weatherTime ->
            if (weatherTime.isNotEmpty()) {
                weatherLastUpdatedTextView.text = "Last Updated: $weatherTime"
            }
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
    
    private fun updateWeatherUI(weather: Weather?) {
        if (weather != null) {
            weatherTitleTextView.text = weather.title
            weatherDescriptionTextView.text = weather.description
        } else {
            weatherTitleTextView.text = "No weather information available"
            weatherDescriptionTextView.text = ""
        }
    }
    
    private fun checkForUpdates() {
        lifecycleScope.launch {
            val updateResult = appUpdateChecker.checkForUpdate()
            appUpdateChecker.showUpdateDialog(updateResult)
        }
    }
    
    private fun listenForLastUpdatedTimes() {
        val database = FirebaseDatabase.getInstance().reference.child("datas")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stocksTimestamp = snapshot.child("stocks").child("gear").child("updatedAt").getValue(Long::class.java) ?: 0L
                val eggsTimestamp = snapshot.child("eggs").child("updatedAt").getValue(Long::class.java) ?: 0L
                
                // Convert timestamps to formatted dates
                val stocksTime = timestampToFormattedDate(stocksTimestamp)
                val eggsTime = timestampToFormattedDate(eggsTimestamp)
                
                viewModel.updateLastUpdated(stocksTime, eggsTime)
                // Weather is no longer available in the new structure
                viewModel.updateWeatherLastUpdated("")
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
    
    /**
     * Convert timestamp to formatted date string
     */
    private fun timestampToFormattedDate(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    private fun requestPermissions() {
        // Request notification permission if not granted
        if (!permissionHandler.hasNotificationPermission()) {
            permissionHandler.showNotificationPermissionDialog(this)
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        (application as GaggyApplication).requestBatteryOptimizationExemption()
    }
    
    private fun requestAutoStartPermission() {
        val sharedPreferences = getSharedPreferences("GaggyPrefs", Context.MODE_PRIVATE)
        val autoStartRequested = sharedPreferences.getBoolean("autoStartRequested", false)
        
        if (!autoStartRequested) {
            com.dainsleif.gaggy.util.AutoStartHelper.getInstance().getAutoStartPermission(this)
            sharedPreferences.edit().putBoolean("autoStartRequested", true).apply()
        }
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