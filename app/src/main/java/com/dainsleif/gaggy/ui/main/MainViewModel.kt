package com.dainsleif.gaggy.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dainsleif.gaggy.data.ItemRepository
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.data.models.ItemType
import com.dainsleif.gaggy.data.models.Weather
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
    
    private val _weather = MutableLiveData<Weather?>()
    val weather: LiveData<Weather?> = _weather
    
    private val _lastUpdated = MutableLiveData<Pair<String, String>>()
    val lastUpdated: LiveData<Pair<String, String>> = _lastUpdated
    
    private val _weatherLastUpdated = MutableLiveData<String>()
    val weatherLastUpdated: LiveData<String> = _weatherLastUpdated
    
    companion object {
        private const val TAG = "MainViewModel"
    }
    
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
            onWeatherUpdated = { weather ->
                _weather.value = weather
                checkForWeatherNotification(weather)
            }
        )
    }
    
    /**
     * Check if any items need notifications
     */
    private fun checkForNotifications(items: List<Item>, itemType: ItemType) {
        // Filter items that need notifications (have quantity > 0 and notifications enabled)
        val itemsToNotify = items.filter { item ->
            item.quantity > 0 && itemRepository.isNotificationEnabled(item.name)
        }
        
        // If there are items to notify, create notifications
        if (itemsToNotify.isNotEmpty()) {
            // Create notifications for each item
            itemsToNotify.forEach { item ->
                notificationHelper.createItemNotification(item)
            }
            
            // Log the items being notified
            val itemNames = itemsToNotify.joinToString(", ") { it.name }
            Log.d(TAG, "Sending notifications for: $itemNames")
        }
    }
    
    /**
     * Check if weather needs notification
     */
    private fun checkForWeatherNotification(weather: Weather) {
        val weatherTitle = weather.title
        // Check if notification is enabled for this weather type
        if (itemRepository.isNotificationEnabled(weatherTitle)) {
            notificationHelper.createWeatherNotification(weather)
        }
    }
    
    /**
     * Update last updated times
     */
    fun updateLastUpdated(stocksTime: String, eggsTime: String) {
        _lastUpdated.value = Pair(stocksTime, eggsTime)
        itemRepository.updateCurrentLastUpdated(stocksTime, eggsTime)
    }
    
    /**
     * Update weather last updated time
     */
    fun updateWeatherLastUpdated(weatherTime: String) {
        _weatherLastUpdated.value = weatherTime
        itemRepository.updateCurrentLastUpdated(
            _lastUpdated.value?.first ?: "",
            _lastUpdated.value?.second ?: "",
            weatherTime
        )
    }
} 