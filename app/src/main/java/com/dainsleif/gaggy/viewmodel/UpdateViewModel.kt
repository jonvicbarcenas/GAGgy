package com.dainsleif.gaggy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dainsleif.gaggy.model.VersionData
import com.dainsleif.gaggy.service.UpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _versionData = MutableStateFlow<VersionData?>(null)
    val versionData: StateFlow<VersionData?> = _versionData.asStateFlow()
    
    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            _error.value = null
            
            UpdateService.checkForUpdates(getApplication()).fold(
                onSuccess = { versionData ->
                    _versionData.value = versionData
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
} 