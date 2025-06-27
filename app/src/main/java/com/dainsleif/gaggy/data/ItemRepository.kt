package com.dainsleif.gaggy.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.data.models.ItemType
import com.dainsleif.gaggy.data.models.Weather
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

/**
 * Repository class to handle all data operations related to items
 */
class ItemRepository private constructor(context: Context) {
    private val database = FirebaseDatabase.getInstance().reference.child("datas")
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("Notifications", Context.MODE_PRIVATE)
    private val lastUpdatedPrefs: SharedPreferences = context.getSharedPreferences("LastUpdated", Context.MODE_PRIVATE)
    private val notificationStatePrefs: SharedPreferences = context.getSharedPreferences("NotificationState", Context.MODE_PRIVATE)
    
    // Store previous items to detect changes
    private var previousItems = mapOf<ItemType, List<Item>>(
        ItemType.GEAR to emptyList(),
        ItemType.SEED to emptyList(),
        ItemType.EGG to emptyList()
    )
    
    // Store current weather information
    private var _currentWeather: Weather? = null
    
    // Store current last updated times
    private var currentLastUpdated = mapOf<ItemType, String>(
        ItemType.GEAR to "",
        ItemType.SEED to "",
        ItemType.EGG to "",
        ItemType.WEATHER to ""
    )
    
    companion object {
        private const val TAG = "ItemRepository"
        
        // Singleton instance
        @Volatile
        private var INSTANCE: ItemRepository? = null
        
        // Get singleton instance
        fun getInstance(context: Context): ItemRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ItemRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Lists of available items
        val GEARS = listOf(
            "Basic Sprinkler",
            "Advanced Sprinkler",
            "Godly Sprinkler",
            "Lightning Rod",
            "Master Sprinkler",
            "Trowel",
            "Recall Wrench",
            "Favorite Tool",
            "Watering Can",
            "Friendship Pot",
            "Cleaning Spray",
            "Tanning Mirror",
            "Harvest Tool"
        )
        
        val SEEDS = listOf(
            "Cauliflower",
            "Watermelon",
            "Green Apple",
            "Avocado",
            "Banana",
            "Pineapple",
            "Kiwi",
            "Bell Pepper",
            "Prickly Pear",
            "Loquat",
            "Feijoa",
            "Sugar Apple"
        )
        
        val EGGS = listOf(
            "Common Egg",
            "Common Summer Egg",
            "Uncommon Egg",
            "Rare Egg",
            "Rare Summer Egg",
            "Legendary Egg",
            "Bug Egg",
            "Mythical Egg",
            "Paradise Egg"
        )
        
        val WEATHER_ALERTS = listOf(
            "Disco",
            "Blackhole",
            "Jandelstorm",
            "Volcano",
            "Chocolate Rain",
            "Windy",
            "Tornado",
            "Raining",
            "Thunder",
            "Snow",
            "Night",
            "Blood Moon",
            "Meteor Shower"
        )
    }
    
    /**
     * Saves notification preferences for an item
     */
    fun saveItemNotificationPreference(itemName: String, enabled: Boolean) {
        sharedPreferences.edit().putBoolean(itemName, enabled).apply()
    }
    
    /**
     * Checks if notifications are enabled for an item
     */
    fun isNotificationEnabled(itemName: String): Boolean {
        return sharedPreferences.getBoolean(itemName, false)
    }
    
    /**
     * Save the last updated time for an item
     */
    fun saveItemLastUpdatedTime(itemName: String, lastUpdated: String) {
        lastUpdatedPrefs.edit().putString(itemName, lastUpdated).apply()
        Log.d(TAG, "Saved last updated time for $itemName: $lastUpdated")
    }
    
    /**
     * Get the saved last updated time for an item
     */
    fun getItemLastUpdatedTime(itemName: String): String {
        return lastUpdatedPrefs.getString(itemName, "") ?: ""
    }
    
    /**
     * Records that a notification has been shown for a specific timestamp
     */
    fun markNotificationShownForTimestamp(itemType: ItemType, timestamp: String) {
        val key = getNotificationStateKey(itemType, timestamp)
        notificationStatePrefs.edit().putBoolean(key, true).apply()
        Log.d(TAG, "Marked notification as shown for $itemType at timestamp: $timestamp")
    }
    
    /**
     * Check if a notification has already been shown for this timestamp
     */
    fun hasNotificationBeenShown(itemType: ItemType, timestamp: String): Boolean {
        val key = getNotificationStateKey(itemType, timestamp)
        return notificationStatePrefs.getBoolean(key, false)
    }
    
    /**
     * Create a unique key for storing notification state
     */
    private fun getNotificationStateKey(itemType: ItemType, timestamp: String): String {
        return "${itemType.name}_$timestamp"
    }
    
    /**
     * Update current last updated times
     */
    fun updateCurrentLastUpdated(stocksTimestamp: Long, eggsTimestamp: Long, weatherTimestamp: Long = 0) {
        val stocksTime = timestampToFormattedDate(stocksTimestamp)
        val eggsTime = timestampToFormattedDate(eggsTimestamp)
        val weatherTime = if (weatherTimestamp > 0) timestampToFormattedDate(weatherTimestamp) else ""
        
        currentLastUpdated = mapOf(
            ItemType.GEAR to stocksTime,
            ItemType.SEED to stocksTime,
            ItemType.EGG to eggsTime,
            ItemType.WEATHER to weatherTime
        )
    }
    
    /**
     * Convert timestamp to formatted date string
     */
    private fun timestampToFormattedDate(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Get current last updated time for an item type
     */
    fun getCurrentLastUpdatedTime(itemType: ItemType): String {
        return currentLastUpdated[itemType] ?: ""
    }
    
    /**
     * Check if an item has a newer update than the saved one
     */
    fun hasNewerUpdate(itemName: String, itemType: ItemType): Boolean {
        val savedTime = getItemLastUpdatedTime(itemName)
        val currentTime = getCurrentLastUpdatedTime(itemType)
        
        // If no saved time, or current time is different and not empty
        return savedTime.isEmpty() || (currentTime.isNotEmpty() && currentTime != savedTime)
    }
    
    /**
     * Get all items with their notification preference status
     */
    fun getAllItemsWithNotificationStatus(itemType: ItemType): List<Pair<String, Boolean>> {
        val items = when (itemType) {
            ItemType.GEAR -> GEARS
            ItemType.SEED -> SEEDS
            ItemType.EGG -> EGGS
            ItemType.WEATHER -> WEATHER_ALERTS
            else -> emptyList()
        }
        
        return items.map { itemName ->
            Pair(itemName, isNotificationEnabled(itemName))
        }
    }
    
    /**
     * Listens for stock changes from Firebase
     */
    fun listenForStockChanges(
        onGearUpdated: (List<Item>, Boolean) -> Unit = { _, _ -> },
        onSeedUpdated: (List<Item>, Boolean) -> Unit = { _, _ -> },
        onEggUpdated: (List<Item>, Boolean) -> Unit = { _, _ -> },
        onWeatherUpdated: (Weather) -> Unit = { }
    ) {
        // Listen for all data changes
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Process data and detect changes
                val gearItems = processGearStock(snapshot)
                val seedItems = processSeedStock(snapshot)
                val eggItems = processEggStock(snapshot)
                val weather = processWeather(snapshot)
                
                // Get the timestamp data
                val stocksTimestamp = snapshot.child("stocks").child("gear").child("updatedAt").getValue(Long::class.java) ?: 0L
                val eggsTimestamp = snapshot.child("eggs").child("updatedAt").getValue(Long::class.java) ?: 0L
                // Weather is not in the new structure, use 0 as default
                val weatherTimestamp = 0L
                
                // Update current timestamps
                updateCurrentLastUpdated(stocksTimestamp, eggsTimestamp, weatherTimestamp)
                
                // Check for any items with enabled notifications
                val hasEnabledGearItems = gearItems.any { isNotificationEnabled(it.name) && it.quantity > 0 }
                val hasEnabledSeedItems = seedItems.any { isNotificationEnabled(it.name) && it.quantity > 0 }
                val hasEnabledEggItems = eggItems.any { isNotificationEnabled(it.name) && it.quantity > 0 }
                
                // Check if notifications have already been shown for these timestamps
                val gearNotificationShown = hasNotificationBeenShown(ItemType.GEAR, stocksTimestamp.toString())
                val seedNotificationShown = hasNotificationBeenShown(ItemType.SEED, stocksTimestamp.toString())
                val eggNotificationShown = hasNotificationBeenShown(ItemType.EGG, eggsTimestamp.toString())
                val weatherNotificationShown = weather != null && hasNotificationBeenShown(ItemType.WEATHER, weatherTimestamp.toString())
                
                // Determine if there are actual changes in items with notifications enabled that haven't been notified yet
                val gearChanged = hasEnabledGearItems && hasItemsChanged(gearItems, previousItems[ItemType.GEAR] ?: emptyList()) && !gearNotificationShown
                val seedChanged = hasEnabledSeedItems && hasItemsChanged(seedItems, previousItems[ItemType.SEED] ?: emptyList()) && !seedNotificationShown
                val eggChanged = hasEnabledEggItems && hasItemsChanged(eggItems, previousItems[ItemType.EGG] ?: emptyList()) && !eggNotificationShown
                
                // Update previous items after checking
                previousItems = mapOf(
                    ItemType.GEAR to gearItems,
                    ItemType.SEED to seedItems,
                    ItemType.EGG to eggItems
                )
                
                // Update current weather
                _currentWeather = weather
                
                // Notify with changes
                onGearUpdated(gearItems, gearChanged)
                onSeedUpdated(seedItems, seedChanged)
                onEggUpdated(eggItems, eggChanged)
                if (weather != null) {
                    // Only notify for weather if notification hasn't been shown
                    val shouldNotifyWeather = !weatherNotificationShown
                    if (shouldNotifyWeather) {
                        onWeatherUpdated(weather)
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
            }
        })
    }
    
    // Check if there are any relevant changes in item quantities
    private fun hasItemsChanged(newItems: List<Item>, oldItems: List<Item>): Boolean {
        // If sizes are different, something changed
        if (newItems.size != oldItems.size) return true
        
        // Create maps for easier comparison
        val oldItemMap = oldItems.associateBy { it.name }
        val newItemMap = newItems.associateBy { it.name }
        
        // Check for new items or quantity changes in items with notifications enabled
        val hasNewOrChangedItems = newItems.any { newItem ->
            if (isNotificationEnabled(newItem.name) && newItem.quantity > 0) {
                val oldItem = oldItemMap[newItem.name]
                // Either the item is new, or its quantity changed
                oldItem == null || oldItem.quantity != newItem.quantity
            } else false
        }
        
        // Check for items that disappeared
        val hasRemovedItems = oldItems.any { oldItem ->
            if (isNotificationEnabled(oldItem.name) && oldItem.quantity > 0) {
                val newItem = newItemMap[oldItem.name]
                // Item was removed or quantity changed to 0
                newItem == null || newItem.quantity == 0
            } else false
        }
        
        return hasNewOrChangedItems || hasRemovedItems
    }
    
    private fun processGearStock(snapshot: DataSnapshot): List<Item> {
        try {
            val gearItems = mutableListOf<Item>()
            val gearSnapshot = snapshot.child("stocks").child("gear").child("items")
            
            gearSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                gearItems.add(Item(name, quantity, ItemType.GEAR))
            }
            
            return gearItems
        } catch (e: Exception) {
            Log.e(TAG, "Error processing gear stock: ${e.message}")
            return emptyList()
        }
    }
    
    private fun processSeedStock(snapshot: DataSnapshot): List<Item> {
        try {
            val seedItems = mutableListOf<Item>()
            val seedSnapshot = snapshot.child("stocks").child("seeds").child("items")
            
            seedSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                seedItems.add(Item(name, quantity, ItemType.SEED))
            }
            
            return seedItems
        } catch (e: Exception) {
            Log.e(TAG, "Error processing seed stock: ${e.message}")
            return emptyList()
        }
    }
    
    private fun processEggStock(snapshot: DataSnapshot): List<Item> {
        try {
            val eggItems = mutableListOf<Item>()
            val eggSnapshot = snapshot.child("eggs").child("items")
            
            eggSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                eggItems.add(Item(name, quantity, ItemType.EGG))
            }
            
            return eggItems
        } catch (e: Exception) {
            Log.e(TAG, "Error processing egg stock: ${e.message}")
            return emptyList()
        }
    }
    
    /**
     * Process weather data from Firebase
     * Note: Weather is not present in the new structure, this will return null
     */
    private fun processWeather(snapshot: DataSnapshot): Weather? {
        // Weather is not in the new structure
        return null
    }
    
    /**
     * Get current weather information
     * Note: Weather is not present in the new structure, this will return null
     */
    suspend fun getWeather(): Weather? {
        // Weather is not in the new structure
        return null
    }
    
    /**
     * Get the currently cached weather information
     */
    fun getCurrentWeather(): Weather? {
        return _currentWeather
    }
    
    /**
     * Get current gear items
     */
    suspend fun getGearItems(): List<Item> {
        val items = mutableListOf<Item>()
        try {
            val dataSnapshot = database.child("stocks").child("gear").child("items").get().await()
            
            dataSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                items.add(Item(name, quantity, ItemType.GEAR))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting gear items: ${e.message}")
        }
        
        return items
    }
    
    /**
     * Get current seed items
     */
    suspend fun getSeedItems(): List<Item> {
        val items = mutableListOf<Item>()
        try {
            val dataSnapshot = database.child("stocks").child("seeds").child("items").get().await()
            
            dataSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                items.add(Item(name, quantity, ItemType.SEED))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting seed items: ${e.message}")
        }
        
        return items
    }
    
    /**
     * Get current egg items
     */
    suspend fun getEggItems(): List<Item> {
        val items = mutableListOf<Item>()
        try {
            val dataSnapshot = database.child("eggs").child("items").get().await()
            
            dataSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                items.add(Item(name, quantity, ItemType.EGG))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting egg items: ${e.message}")
        }
        
        return items
    }
} 