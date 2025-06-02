package com.dainsleif.gaggy.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dainsleif.gaggy.data.ItemRepository
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.data.models.ItemType
import com.dainsleif.gaggy.notifications.NotificationHelper

/**
 * ViewModel for the main screen
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val itemRepository = ItemRepository.getInstance(application)
    private val notificationHelper = NotificationHelper.getInstance(application)
    
    private val _gearItems = MutableLiveData<List<Item>>()
    val gearItems: LiveData<List<Item>> = _gearItems
    
    private val _seedItems = MutableLiveData<List<Item>>()
    val seedItems: LiveData<List<Item>> = _seedItems
    
    private val _eggItems = MutableLiveData<List<Item>>()
    val eggItems: LiveData<List<Item>> = _eggItems
    
    private val _honeyItems = MutableLiveData<List<Item>>()
    val honeyItems: LiveData<List<Item>> = _honeyItems
    
    private val _lastUpdated = MutableLiveData<Triple<String, String, String>>()
    val lastUpdated: LiveData<Triple<String, String, String>> = _lastUpdated
    
    init {
        startListeningForStockChanges()
    }
    
    /**
     * Start listening for stock changes from Firebase
     */
    private fun startListeningForStockChanges() {
        itemRepository.listenForStockChanges(
            onGearUpdated = { gearItems, isChanged ->
                _gearItems.value = gearItems
                if (isChanged) {
                    checkForNotifications(gearItems, ItemType.GEAR)
                }
            },
            onSeedUpdated = { seedItems, isChanged ->
                _seedItems.value = seedItems
                if (isChanged) {
                    checkForNotifications(seedItems, ItemType.SEED)
                }
            },
            onEggUpdated = { eggItems, isChanged ->
                _eggItems.value = eggItems
                if (isChanged) {
                    checkForNotifications(eggItems, ItemType.EGG)
                }
            },
            onHoneyUpdated = { honeyItems, isChanged ->
                _honeyItems.value = honeyItems
                if (isChanged) {
                    checkForNotifications(honeyItems, ItemType.HONEY)
                }
            }
        )
    }
    
    /**
     * Check if any items need notifications
     */
    private fun checkForNotifications(items: List<Item>, itemType: ItemType) {
        items.forEach { item ->
            // If quantity is greater than 0 and notifications are enabled for this item, send notification
            if (item.quantity > 0 && itemRepository.isNotificationEnabled(item.name)) {
                notificationHelper.createItemNotification(item)
            }
        }
    }
    
    /**
     * Update last updated times
     */
    fun updateLastUpdated(stocksTime: String, eggsTime: String, honeyTime: String) {
        _lastUpdated.value = Triple(stocksTime, eggsTime, honeyTime)
    }
} 