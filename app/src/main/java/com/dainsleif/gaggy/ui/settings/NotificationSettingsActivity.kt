package com.dainsleif.gaggy.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import androidx.lifecycle.ViewModelProvider
import com.dainsleif.gaggy.R
import com.dainsleif.gaggy.data.models.ItemType
import com.dainsleif.gaggy.notifications.NotificationChannelManager

class NotificationSettingsActivity : AppCompatActivity() {
    private lateinit var settingsLayout: LinearLayout
    private lateinit var viewModel: NotificationSettingsViewModel
    private lateinit var notificationChannelManager: NotificationChannelManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NotificationSettingsViewModel::class.java]
        
        // Initialize notification channels
        notificationChannelManager = NotificationChannelManager(this)
        notificationChannelManager.createAllChannels()
        
        // Initialize UI components
        settingsLayout = findViewById(R.id.settingsLayout)
        
        // Set up back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        
        // Observe ViewModel data
        observeViewModel()
        
        // Add button to open battery optimization settings
        addBatteryOptimizationButton()
    }
    
    private fun observeViewModel() {
        // Observe gear items
        viewModel.gearItems.observe(this) { gearItems ->
            setupItemsSection("Gear Notification Settings", gearItems, ItemType.GEAR)
        }
        
        // Observe seed items
        viewModel.seedItems.observe(this) { seedItems ->
            setupItemsSection("Seed Notification Settings", seedItems, ItemType.SEED)
        }
        
        // Observe egg items
        viewModel.eggItems.observe(this) { eggItems ->
            setupItemsSection("Egg Notification Settings", eggItems, ItemType.EGG)
        }
        
        // Observe honey items
        viewModel.honeyItems.observe(this) { honeyItems ->
            setupItemsSection("Honey Notification Settings", honeyItems, ItemType.HONEY)
        }
    }
    
    private fun setupItemsSection(title: String, items: List<Pair<String, Boolean>>, itemType: ItemType) {
        // Add header
        val header = TextView(this)
        header.text = title
        header.textSize = 20f
        header.setPadding(0, 16, 0, 16)
        settingsLayout.addView(header)
        
        // Add a switch for each item
        items.forEachIndexed { index, (itemName, isEnabled) ->
            val switchLayout = LinearLayout(this)
            switchLayout.orientation = LinearLayout.HORIZONTAL
            switchLayout.setPadding(16, 16, 16, 16)
            
            val itemText = TextView(this)
            itemText.text = itemName
            itemText.textSize = 16f
            itemText.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            
            val switch = Switch(this)
            switch.isChecked = isEnabled
            switch.tag = itemName
            switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                viewModel.updateNotificationSetting(itemName, isChecked)
                Toast.makeText(
                    this,
                    "$itemName notifications ${if (isChecked) "enabled" else "disabled"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            switchLayout.addView(itemText)
            switchLayout.addView(switch)
            
            settingsLayout.addView(switchLayout)
            
            // Add a divider except for the last item
            if (index < items.size - 1) {
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
} 