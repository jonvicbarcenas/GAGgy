package com.dainsleif.gaggy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dainsleif.gaggy.model.VersionData
import com.dainsleif.gaggy.service.UpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.dainsleif.gaggy.BuildConfig
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _versionData = MutableStateFlow<VersionData?>(null)
    val versionData: StateFlow<VersionData?> = _versionData.asStateFlow()
    
    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: StateFlow<Boolean> = _updateAvailable.asStateFlow()
    
    // Get current app version from BuildConfig
    private val currentAppVersion = BuildConfig.VERSION_NAME  // ✅ This works without import in same module

    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            _error.value = null
            
            UpdateService.checkForUpdates(getApplication()).fold(
                onSuccess = { versionData ->
                    _versionData.value = versionData
                    // Compare versions to determine if update is needed
                    _updateAvailable.value = isNewerVersion(versionData.version, currentAppVersion)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                }
            )
            
            _isCheckingForUpdates.value = false
        }
    }
    
    fun forceUpdate() {
        _versionData.value?.url?.let { url ->
            UpdateService.openUpdateUrl(getApplication(), url)
        }
    }
    
    /**
     * Compare version strings to determine if remote version is newer
     * @param remoteVersion The version from the server (e.g. "v1.1.2")
     * @param localVersion The current app version (e.g. "2.0.1")
     * @return true if remote version is newer than local version
     */
    private fun isNewerVersion(remoteVersion: String, localVersion: String): Boolean {
        // Remove 'v' prefix if it exists
        val cleanRemoteVersion = remoteVersion.removePrefix("v")
        val cleanLocalVersion = localVersion.removePrefix("v")
        
        // Split version strings into components
        val remoteParts = cleanRemoteVersion.split(".")
        val localParts = cleanLocalVersion.split(".")
        
        // Compare each component
        for (i in 0 until minOf(remoteParts.size, localParts.size)) {
            val remotePart = remoteParts[i].toIntOrNull() ?: 0
            val localPart = localParts[i].toIntOrNull() ?: 0
            
            if (remotePart > localPart) {
                return true
            } else if (remotePart < localPart) {
                return false
            }
        }
        
        // If we get here, all compared components are equal
        // If remote has more components, it's newer
        return remoteParts.size > localParts.size
    }
} 