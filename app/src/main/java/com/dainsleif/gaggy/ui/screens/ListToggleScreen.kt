package com.dainsleif.gaggy.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Constant for SharedPreferences
private const val PREFS_NAME = "GardenEggPrefs"
private const val PREF_PREFIX_EGG = "egg_"
private const val PREF_PREFIX_GEAR = "gear_"
private const val PREF_PREFIX_SETTING = "setting_"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListToggleScreen(onBackPressed: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Egg Notifications",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Common Egg
            EggToggleItem(name = "Common Egg", context = context)
            
            // Rare Egg
            EggToggleItem(name = "Rare Egg", context = context)
            
            // Uncommon Egg
            EggToggleItem(name = "Uncommon Egg", context = context)
            
            // Legendary Egg
            EggToggleItem(name = "Legendary Egg", context = context)
            
            // Bug Egg
            EggToggleItem(name = "Bug Egg", context = context)
            
            // Mythical Egg
            EggToggleItem(name = "Mythical Egg", context = context)
            
            // Paradise Egg
            EggToggleItem(name = "Paradise Egg", context = context)
            
            // Common Summer Egg
            EggToggleItem(name = "Common Summer Egg", context = context)
            
            // Rare Summer Egg
            EggToggleItem(name = "Rare Summer Egg", context = context)
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // Gear Notifications Section
            Text(
                text = "Gear Notifications",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Gear items
            GearToggleItem(name = "Watering Can", context = context)
            GearToggleItem(name = "Trowel", context = context)
            GearToggleItem(name = "Favorite Tool", context = context)
            GearToggleItem(name = "Basic Sprinkler", context = context)
            GearToggleItem(name = "Godly Sprinkler", context = context)
            GearToggleItem(name = "Advanced Sprinkler", context = context)
            GearToggleItem(name = "Master Sprinkler", context = context)
            GearToggleItem(name = "Lightning Rod", context = context)
            GearToggleItem(name = "Recall Wrench", context = context)
            GearToggleItem(name = "Harvest Tool", context = context)
            GearToggleItem(name = "Friendship Pot", context = context)
            GearToggleItem(name = "Cleaning Spray", context = context)
            GearToggleItem(name = "Tanning Mirror", context = context)
        }
    }
}

@Composable
fun EggToggleItem(name: String, context: Context) {
    val prefKey = PREF_PREFIX_EGG + name.replace(" ", "_").lowercase()
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Load saved preference or default to false
    var isEnabled by remember { mutableStateOf(sharedPrefs.getBoolean(prefKey, false)) }
    
    // Save preference when toggle changes
    DisposableEffect(isEnabled) {
        sharedPrefs.edit().putBoolean(prefKey, isEnabled).apply()
        onDispose { }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { newValue ->
                    isEnabled = newValue
                    // Save to SharedPreferences
                    sharedPrefs.edit().putBoolean(prefKey, newValue).apply()
                }
            )
        }
    }
}

@Composable
fun GearToggleItem(name: String, context: Context) {
    val prefKey = PREF_PREFIX_GEAR + name.replace(" ", "_").lowercase()
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Load saved preference or default to false
    var isEnabled by remember { mutableStateOf(sharedPrefs.getBoolean(prefKey, false)) }
    
    // Save preference when toggle changes
    DisposableEffect(isEnabled) {
        sharedPrefs.edit().putBoolean(prefKey, isEnabled).apply()
        onDispose { }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { newValue ->
                    isEnabled = newValue
                    // Save to SharedPreferences
                    sharedPrefs.edit().putBoolean(prefKey, newValue).apply()
                }
            )
        }
    }
}
