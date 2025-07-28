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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Constant for SharedPreferences
private const val PREFS_NAME = "GardenEggPrefs"
private const val PREF_PREFIX_EGG = "egg_"
private const val PREF_PREFIX_GEAR = "gear_"
private const val PREF_PREFIX_SEED = "seed_"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListToggleScreen(onBackPressed: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("Eggs", "Seeds", "Gear")
    
    // Custom green colors for tabs
    val tabBackgroundColor = Color(0xFF4CAF50)  // Medium green
    val tabSelectedContentColor = Color.White
    val tabUnselectedContentColor = Color(0xFFE8F5E9)  // Light green
    val tabIndicatorColor = Color(0xFF1B5E20)  // Dark green
    
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
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = tabBackgroundColor,
                contentColor = tabSelectedContentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = tabIndicatorColor
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) tabSelectedContentColor else tabUnselectedContentColor
                            ) 
                        }
                    )
                }
            }
            
            when (selectedTabIndex) {
                0 -> EggNotificationsTab(context, scrollState)
                1 -> SeedNotificationsTab(context, scrollState)
                2 -> GearNotificationsTab(context, scrollState)
            }
        }
    }
}

@Composable
fun EggNotificationsTab(context: Context, scrollState: androidx.compose.foundation.ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Egg Notifications",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
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

        // Bee Egg
        EggToggleItem(name = "Bee Egg", context = context)
        
        // Mythical Egg
        EggToggleItem(name = "Mythical Egg", context = context)
        
        // Paradise Egg
        EggToggleItem(name = "Paradise Egg", context = context)
        
        // Common Summer Egg
        EggToggleItem(name = "Common Summer Egg", context = context)
        
        // Rare Summer Egg
        EggToggleItem(name = "Rare Summer Egg", context = context)
    }
}

@Composable
fun SeedNotificationsTab(context: Context, scrollState: androidx.compose.foundation.ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Seed Notifications",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Seed items
        SeedToggleItem(name = "Watermelon", context = context)
        SeedToggleItem(name = "Pumpkin", context = context)
        SeedToggleItem(name = "Coconut", context = context)
        SeedToggleItem(name = "Cactus", context = context)
        SeedToggleItem(name = "Dragon Fruit", context = context)
        SeedToggleItem(name = "Mango", context = context)
        SeedToggleItem(name = "Grape", context = context)
        SeedToggleItem(name = "Mushroom", context = context)
        SeedToggleItem(name = "Pepper", context = context)
        SeedToggleItem(name = "Cacao", context = context)
        SeedToggleItem(name = "Beanstalk", context = context)
        SeedToggleItem(name = "Ember Lily", context = context)
        SeedToggleItem(name = "Sugar Apple", context = context)
        SeedToggleItem(name = "Burning Bud", context = context)
        SeedToggleItem(name = "Elder Strawberry", context = context)
        SeedToggleItem(name = "Giant Pinecone", context = context)
    }
}

@Composable
fun GearNotificationsTab(context: Context, scrollState: androidx.compose.foundation.ScrollState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Gear Notifications",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Gear items
        GearToggleItem(name = "Watering Can", context = context)
        GearToggleItem(name = "Levelup Lollipop", context = context)
        GearToggleItem(name = "Medium Toy", context = context)
        GearToggleItem(name = "Medium Treat", context = context)
        GearToggleItem(name = "Trowel", context = context)
        GearToggleItem(name = "Favorite Tool", context = context)
        GearToggleItem(name = "Basic Sprinkler", context = context)
        GearToggleItem(name = "Godly Sprinkler", context = context)
        GearToggleItem(name = "Advanced Sprinkler", context = context)
        GearToggleItem(name = "Master Sprinkler", context = context)
        GearToggleItem(name = "Magnifying Glass", context = context)
        GearToggleItem(name = "Recall Wrench", context = context)
        GearToggleItem(name = "Harvest Tool", context = context)
        GearToggleItem(name = "Friendship Pot", context = context)
        GearToggleItem(name = "Cleaning Spray", context = context)
        GearToggleItem(name = "Tanning Mirror", context = context)
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

@Composable
fun SeedToggleItem(name: String, context: Context) {
    val prefKey = PREF_PREFIX_SEED + name.replace(" ", "_").lowercase()
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
