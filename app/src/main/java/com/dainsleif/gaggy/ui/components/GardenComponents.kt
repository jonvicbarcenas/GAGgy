package com.dainsleif.gaggy.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dainsleif.gaggy.model.CategoryData
import com.dainsleif.gaggy.model.EggData
import com.dainsleif.gaggy.model.GardenData
import com.dainsleif.gaggy.model.ItemData
import com.dainsleif.gaggy.model.WeatherData
import com.dainsleif.gaggy.model.WeatherItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// Constants for SharedPreferences
private const val PREFS_NAME = "GardenEggPrefs"
private const val PREF_PREFIX_SETTING = "setting_"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenListScreen(
    gardenData: GardenData?,
    isLoading: Boolean,
    error: String?,
    onNotificationClick: () -> Unit,
    onCheckForUpdates: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onBatteryOptimizationClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Text-to-speech state
    var ttsEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("${PREF_PREFIX_SETTING}text_to_speech", true)) 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "GAG Stocks",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(28.dp)
                            .clickable { onNotificationClick() }
                    )
                    
                    Box {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(28.dp)
                                .clickable { showMenu = true }
                        )
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Text-to-Speech toggle
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Text-to-Speech")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (ttsEnabled) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Enabled",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    val newValue = !ttsEnabled
                                    ttsEnabled = newValue
                                    sharedPrefs.edit().putBoolean("${PREF_PREFIX_SETTING}text_to_speech", newValue).apply()
                                }
                            )
                            
                            Divider()
                            
                            DropdownMenuItem(
                                text = { Text("Check for Updates") },
                                onClick = {
                                    showMenu = false
                                    onCheckForUpdates()
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Disable Battery Optimization") },
                                onClick = {
                                    showMenu = false
                                    onBatteryOptimizationClick()
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    showMenu = false
                                    onAboutClick()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                isLoading -> {
                    LoadingIndicator()
                }
                error != null -> {
                    ErrorMessage(error)
                }
                gardenData != null -> {
                    GardenDataContent(gardenData)
                }
                else -> {
                    EmptyState()
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $message",
            color = Color.Red,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No garden data available",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GardenDataContent(gardenData: GardenData) {
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
//                GardenHeader()
            }

            // Weather section
            gardenData.datas.weather?.let { weatherData ->
                item {
                    CategoryHeader(title = "Weather", updatedAt = weatherData.updatedAt)
                }
                
                item {
                    WeatherSection(weatherData = weatherData)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Eggs section
            gardenData.datas.eggs?.let { eggData ->
                item {
                    CategoryHeader(title = "Eggs", updatedAt = eggData.updatedAt)
                }
                
                items(eggData.items) { item ->
                    ItemRow(item = item)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Seeds section
            gardenData.datas.stocks?.seeds?.let { seedsData ->
                item {
                    CategoryHeader(title = "Seeds", updatedAt = seedsData.updatedAt)
                }
                
                items(seedsData.items) { item ->
                    ItemRow(item = item)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Gear section
            gardenData.datas.stocks?.gear?.let { gearData ->
                item {
                    CategoryHeader(title = "Gardening Gear", updatedAt = gearData.updatedAt)
                }
                
                items(gearData.items) { item ->
                    ItemRow(item = item)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GardenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GAG Stocks",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Divider(
            modifier = Modifier.width(180.dp),
            color = MaterialTheme.colorScheme.secondary,
            thickness = 2.dp
        )
    }
}

@Composable
fun CategoryHeader(title: String, updatedAt: Long) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(updatedAt))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Last updated: $formattedDate",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ItemRow(item: ItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored circle indicator with border
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.quantity.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = item.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WeatherSection(weatherData: WeatherData) {
    // Use remember and mutableStateOf to create a state that will trigger recomposition
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Use LaunchedEffect to set up a timer that updates the current time every minute
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(60000) // Update every minute (60000 milliseconds)
            currentTimeMillis = System.currentTimeMillis()
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            for (item in weatherData.items) {
                // Skip the Timestamp field as it's not user-friendly
                if (item.name == "Timestamp") continue
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Highlight the Special field with a different style
                    val isSpecial = item.name == "Special"
                    
                    Text(
                        text = "${item.name}:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpecial) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(100.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Convert Discord timestamp format to relative time if it's an "Ends" field
                    // Using currentTimeMillis in the calculation will cause this to update when the state changes
                    val displayText = if (item.name == "Ends" && item.value.startsWith("<t:") && item.value.contains(":R>")) {
                        try {
                            val timestamp = item.value.substringAfter("<t:").substringBefore(":R>").toLong() * 1000 // Convert to milliseconds
                            val diff = timestamp - currentTimeMillis
                            
                            when {
                                diff < 0 -> {
                                    // Past time
                                    val absDiff = Math.abs(diff)
                                    when {
                                        absDiff < 60000 -> "just now"
                                        absDiff < 3600000 -> "${absDiff / 60000} minutes ago"
                                        absDiff < 86400000 -> "${absDiff / 3600000} hours ago"
                                        absDiff < 604800000 -> "${absDiff / 86400000} days ago"
                                        else -> "${absDiff / 604800000} weeks ago"
                                    }
                                }
                                diff < 60000 -> "in less than a minute"
                                diff < 3600000 -> "in ${diff / 60000} minutes"
                                diff < 86400000 -> "in ${diff / 3600000} hours"
                                diff < 604800000 -> "in ${diff / 86400000} days"
                                else -> "in ${diff / 604800000} weeks"
                            }
                        } catch (e: Exception) {
                            item.value // If parsing fails, show original value
                        }
                    } else {
                        item.value
                    }
                    
                    Text(
                        text = displayText,
                        fontSize = 16.sp,
                        fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSpecial) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (weatherData.items.indexOf(item) < weatherData.items.size - 1 && 
                    weatherData.items.getOrNull(weatherData.items.indexOf(item) + 1)?.name != "Timestamp") {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}