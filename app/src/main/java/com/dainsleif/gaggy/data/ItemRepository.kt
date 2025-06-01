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

/**
 * Repository class to handle all data operations related to items
 */
class ItemRepository private constructor(context: Context) {
    private val database = FirebaseDatabase.getInstance().reference.child("discord_data")
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("Notifications", Context.MODE_PRIVATE)
    
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
        onGearUpdated: (List<Item>) -> Unit = {},
        onSeedUpdated: (List<Item>) -> Unit = {},
        onEggUpdated: (List<Item>) -> Unit = {},
        onHoneyUpdated: (List<Item>) -> Unit = {}
    ) {
        // Listen for all data changes
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                processGearStock(snapshot, onGearUpdated)
                processSeedStock(snapshot, onSeedUpdated)
                processEggStock(snapshot, onEggUpdated)
                processHoneyStock(snapshot, onHoneyUpdated)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
            }
        })
    }
    
    private fun processGearStock(snapshot: DataSnapshot, callback: (List<Item>) -> Unit) {
        try {
            val gearItems = mutableListOf<Item>()
            val gearSnapshot = snapshot.child("stocks").child("GEAR STOCK")
            
            gearSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                gearItems.add(Item(name, quantity, ItemType.GEAR))
            }
            
            callback(gearItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing gear stock: ${e.message}")
            callback(emptyList())
        }
    }
    
    private fun processSeedStock(snapshot: DataSnapshot, callback: (List<Item>) -> Unit) {
        try {
            val seedItems = mutableListOf<Item>()
            val seedSnapshot = snapshot.child("stocks").child("SEEDS STOCK")
            
            seedSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                seedItems.add(Item(name, quantity, ItemType.SEED))
            }
            
            callback(seedItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing seed stock: ${e.message}")
            callback(emptyList())
        }
    }
    
    private fun processEggStock(snapshot: DataSnapshot, callback: (List<Item>) -> Unit) {
        try {
            val eggItems = mutableListOf<Item>()
            val eggSnapshot = snapshot.child("eggs").child("EGG STOCK")
            
            eggSnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                eggItems.add(Item(name, quantity, ItemType.EGG))
            }
            
            callback(eggItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing egg stock: ${e.message}")
            callback(emptyList())
        }
    }
    
    private fun processHoneyStock(snapshot: DataSnapshot, callback: (List<Item>) -> Unit) {
        try {
            val honeyItems = mutableListOf<Item>()
            val honeySnapshot = snapshot.child("honeyStocks").child("HONEY STOCK")
            
            honeySnapshot.children.forEach { itemSnapshot ->
                val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                
                honeyItems.add(Item(name, quantity, ItemType.HONEY))
            }
            
            callback(honeyItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing honey stock: ${e.message}")
            callback(emptyList())
        }
    }
} 