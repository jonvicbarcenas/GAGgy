package com.dainsleif.gaggy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dainsleif.gaggy.model.GardenData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GardenViewModel : ViewModel() {
    
    private val _gardenData = MutableStateFlow<GardenData?>(null)
    val gardenData: StateFlow<GardenData?> = _gardenData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        fetchGardenData()
    }
    
    fun fetchGardenData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val database = FirebaseDatabase.getInstance()
            val reference = database.getReference("/")
            
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val gardenData = snapshot.getValue(GardenData::class.java)
                        _gardenData.value = gardenData
                        _isLoading.value = false
                    } catch (e: Exception) {
                        _error.value = "Failed to parse data: ${e.message}"
                        _isLoading.value = false
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    _error.value = "Database error: ${error.message}"
                    _isLoading.value = false
                }
            })
        }
    }
} 