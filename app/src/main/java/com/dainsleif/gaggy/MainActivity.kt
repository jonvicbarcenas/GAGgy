package com.dainsleif.gaggy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dainsleif.gaggy.ui.components.GardenListScreen
import com.dainsleif.gaggy.ui.components.UpdateDialog
import com.dainsleif.gaggy.ui.screens.ListToggleScreen
import com.dainsleif.gaggy.ui.theme.GardenAppTheme
import com.dainsleif.gaggy.viewmodel.GardenViewModel
import com.dainsleif.gaggy.viewmodel.UpdateViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    
    private val gardenViewModel: GardenViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()
    
    // Request permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Optional: Handle permission result
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        setContent {
            GardenAppTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false // Use our custom green theme
            ) {
                val navController = rememberNavController()
                
                // Update dialog state
                var showUpdateDialog by remember { mutableStateOf(false) }
                val versionData by updateViewModel.versionData.collectAsState()
                val isCheckingForUpdates by updateViewModel.isCheckingForUpdates.collectAsState()
                val updateError by updateViewModel.error.collectAsState()
                
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        val gardenData by gardenViewModel.gardenData.collectAsState()
                        val isLoading by gardenViewModel.isLoading.collectAsState()
                        val error by gardenViewModel.error.collectAsState()
                        
                        GardenListScreen(
                            gardenData = gardenData,
                            isLoading = isLoading,
                            error = error,
                            onNotificationClick = {
                                navController.navigate("notifications")
                            },
                            onCheckForUpdates = {
                                showUpdateDialog = true
                                updateViewModel.checkForUpdates()
                            }
                        )
                    }
                    
                    composable("notifications") {
                        ListToggleScreen(
                            onBackPressed = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
                
                // Show update dialog if needed
                if (showUpdateDialog) {
                    UpdateDialog(
                        versionData = versionData,
                        isLoading = isCheckingForUpdates,
                        error = updateError,
                        onDismiss = { showUpdateDialog = false },
                        onForceUpdate = {
                            updateViewModel.forceUpdate()
                            showUpdateDialog = false
                        }
                    )
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show permission rationale if needed
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
} 