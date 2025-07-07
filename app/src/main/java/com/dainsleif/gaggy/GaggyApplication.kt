package com.dainsleif.gaggy

import android.app.Application
import com.dainsleif.gaggy.service.DeviceOnlineService
import com.dainsleif.gaggy.service.GardenForegroundService
import com.dainsleif.gaggy.service.UpdateService
import com.google.firebase.FirebaseApp

class GaggyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Start foreground service
        GardenForegroundService.startService(this)
        
        // Schedule hourly update checks
        UpdateService.scheduleUpdateChecks(this)
        
        // Register device for tracking
        DeviceOnlineService.registerDevice(this)
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Stop foreground service
        GardenForegroundService.stopService(this)
        
        // Stop device tracking
        DeviceOnlineService.stopTracking()
    }
} 