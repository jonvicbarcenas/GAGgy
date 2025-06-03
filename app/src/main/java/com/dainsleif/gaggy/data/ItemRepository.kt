package com.dainsleif.gaggy.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.dainsleif.gaggy.data.models.Item
import com.dainsleif.gaggy.data.models.ItemType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

/**
 * Repository class to handle all data operations related to items
 */
class ItemRepository private constructor(context: Context) {
    private val database = FirebaseDatabase.getInstance().reference.child("discord_data")
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("Notifications", Context.MODE_PRIVATE)
    
    // Store previous items to detect changes
    private var previousItems = mapOf<ItemType, List<Item>>(
        ItemType.GEAR to emptyList(),
        ItemType.SEED to emptyList(),
        ItemType.EGG to emptyList(),
        ItemType.HONEY to emptyList()
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
            "Watering Can",
            "Trowel",
            "Favorite Tool",
            "Basic Sprinkler",
            "Godly Sprinkler",
            "Advanced Sprinkler",
            "Master Sprinkler",
            "Lightning Rod",
            "Recall Wrench"
        )
        
        val SEEDS = listOf(
            "Carrot",
            "Strawberry",
            "Blueberry",
            "Orange Tulip",
            "Tomato",
            "Bamboo",
            "Watermelon",
            "Apple",
            "Pepper",
            "Mango",
            "Daffodil",
            "Pumpkin",
            "Corn",
            "Coconut",
            "Cactus",
            "Cacao",
            "Dragon Fruit",
            "Grape",
            "Mushroom",
            "Beanstalk"
        )
        
        val EGGS = listOf(
            "Common Egg",
            "Rare Egg",
            "Uncommon Egg",
            "Legendary Egg",
            "Bug Egg",
            "Mythical Egg"
        )
        
        val HONEY_ITEMS = listOf(
            "Flower Seed Pack",
            "Nectarine Seed",
            "Hive Fruit Seed",
            "Honey Sprinkler",
            "Bee Egg",
            "Bee Crate",
            "Honey Comb",
            "Bee Chair",
            "Honey Torch",
            "Honey WAlkway"
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
     * Get all items with their notification preference status
     */
    fun getAllItemsWithNotificationStatus(itemType: ItemType): List<Pair<String, Boolean>> {
        val items = when (itemType) {
            ItemType.GEAR -> GEARS
            ItemType.SEED -> SEEDS
            ItemType.EGG -> EGGS
            ItemType.HONEY -> HONEY_ITEMS
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
        onHoneyUpdated: (List<Item>, Boolean) -> Unit = { _, _ -> }
    ) {
        // Listen for all data changes
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Process data and detect changes
                val gearItems = processGearStock(snapshot)
                val seedItems = processSeedStock(snapshot)
                val eggItems = processEggStock(snapshot)
                val honeyItems = processHoneyStock(snapshot)
                
                // Check for any items with enabled notifications
                val hasEnabledGearItems = gearItems.any { isNotificationEnabled(it.name) && it.quantity > 0 }
                val hasEnabledSeedItems = seedItems.any { isNotificationEnabled(it.name) && it.quantity > 0 }
                val hasEnabledEggItems = eggItems.any { isNotificationEnabled(it.name) && it.quantity > 0 }
                val hasEnabledHoneyItems = honeyItems.any { isNotificationEnabled(it.name) && it.quantity > 0 }
                
                // Determine if there are actual changes in items with notifications enabled
                val gearChanged = hasEnabledGearItems && hasItemsChanged(gearItems, previousItems[ItemType.GEAR] ?: emptyList())
                val seedChanged = hasEnabledSeedItems && hasItemsChanged(seedItems, previousItems[ItemType.SEED] ?: emptyList())
                val eggChanged = hasEnabledEggItems && hasItemsChanged(eggItems, previousItems[ItemType.EGG] ?: emptyList())
                val honeyChanged = hasEnabledHoneyItems && hasItemsChanged(honeyItems, previousItems[ItemType.HONEY] ?: emptyList())
                
                // Update previous items after checking
                previousItems = mapOf(
                    ItemType.GEAR to gearItems,
                    ItemType.SEED to seedItems,
                    ItemType.EGG to eggItems,
                    ItemType.HONEY to honeyItems
                )
                
                // Notify with changes
                onGearUpdated(gearItems, gearChanged)
                onSeedUpdated(seedItems, seedChanged)
                onEggUpdated(eggItems, eggChanged)
                onHoneyUpdated(honeyItems, honeyChanged)
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
        
        // Check each item with notifications enabled for quantity changes
        val oldItemMap = oldItems.associateBy { it.name }
        
        return newItems.any { newItem ->
            // Only check items with enabled notifications
            if (isNotificationEnabled(newItem.name) && newItem.quantity > 0) {
                val oldItem = oldItemMap[newItem.name]
                // Either the item is new, or its quantity changed
                oldItem == null || oldItem.quantity != newItem.quantity
            } else false
        }
    }
    
    private fun processGearStock(snapshot: DataSnapshot): List<Item> {
        try {
            val gearItems = mutableListOf<Item>()
            val gearSnapshot = snapshot.child("stocks").child("GEAR STOCK")
            
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
            val seedSnapshot = snapshot.child("stocks").child("SEEDS STOCK")
            
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
            val eggSnapshot = snapshot.child("eggs").child("EGG STOCK")
            
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
    
    private fun processHoneyStock(snapshot: DataSnapshot): List<Item> {
        try {
            val honeyItems = mutableListOf<Item>()
            val honeySnapshot = snapshot.child("honeyStocks").child("HONEY STOCK")
            
            honeySnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                honeyItems.add(Item(name, quantity, ItemType.HONEY))
            }
            
            return honeyItems
        } catch (e: Exception) {
            Log.e(TAG, "Error processing honey stock: ${e.message}")
            return emptyList()
        }
    }
    
    /**
     * Get current gear items
     */
    suspend fun getGearItems(): List<Item> {
        val items = mutableListOf<Item>()
        try {
            val dataSnapshot = database.child("stocks").child("GEAR STOCK").get().await()
            
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
            val dataSnapshot = database.child("stocks").child("SEEDS STOCK").get().await()
            
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
            val dataSnapshot = database.child("eggs").child("EGG STOCK").get().await()
            
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
    
    /**
     * Get current honey items
     */
    suspend fun getHoneyItems(): List<Item> {
        val items = mutableListOf<Item>()
        try {
            val dataSnapshot = database.child("honeyStocks").child("HONEY STOCK").get().await()
            
            dataSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                items.add(Item(name, quantity, ItemType.HONEY))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting honey items: ${e.message}")
        }
        
        return items
    }
} 