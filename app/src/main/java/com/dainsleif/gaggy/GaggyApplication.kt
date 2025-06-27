package com.dainsleif.gaggy

import android.app.Application
import com.dainsleif.gaggy.service.GardenForegroundService
import com.google.firebase.FirebaseApp

class GaggyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Start foreground service
        GardenForegroundService.startService(this)
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Stop foreground service
        GardenForegroundService.stopService(this)
    }
} 