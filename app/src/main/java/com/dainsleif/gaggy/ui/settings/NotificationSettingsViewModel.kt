package com.dainsleif.gaggy.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dainsleif.gaggy.data.ItemRepository
import com.dainsleif.gaggy.data.models.ItemType

/**
 * ViewModel for the notification settings screen
 */
class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val itemRepository = ItemRepository.getInstance(application)
    
    private val _gearItems = MutableLiveData<List<Pair<String, Boolean>>>()
    val gearItems: LiveData<List<Pair<String, Boolean>>> = _gearItems
    
    private val _seedItems = MutableLiveData<List<Pair<String, Boolean>>>()
    val seedItems: LiveData<List<Pair<String, Boolean>>> = _seedItems
    
    private val _eggItems = MutableLiveData<List<Pair<String, Boolean>>>()
    val eggItems: LiveData<List<Pair<String, Boolean>>> = _eggItems
    
    private val _honeyItems = MutableLiveData<List<Pair<String, Boolean>>>()
    val honeyItems: LiveData<List<Pair<String, Boolean>>> = _honeyItems
    
    private val _weatherAlerts = MutableLiveData<List<Pair<String, Boolean>>>()
    val weatherAlerts: LiveData<List<Pair<String, Boolean>>> = _weatherAlerts
    
    init {
        loadAllSettings()
    }
    
    /**
     * Load all notification settings
     */
    fun loadAllSettings() {
        _gearItems.value = itemRepository.getAllItemsWithNotificationStatus(ItemType.GEAR)
        _seedItems.value = itemRepository.getAllItemsWithNotificationStatus(ItemType.SEED)
        _eggItems.value = itemRepository.getAllItemsWithNotificationStatus(ItemType.EGG)
        _honeyItems.value = itemRepository.getAllItemsWithNotificationStatus(ItemType.HONEY)
        _weatherAlerts.value = itemRepository.getAllItemsWithNotificationStatus(ItemType.WEATHER)
    }
    
    /**
     * Update notification setting for an item
     */
    fun updateNotificationSetting(itemName: String, enabled: Boolean) {
        itemRepository.saveItemNotificationPreference(itemName, enabled)
    }
} 