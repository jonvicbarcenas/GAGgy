package com.dainsleif.gaggy.data.models

/**
 * Base data class for items in the game
 */
data class Item(
    val name: String = "",
    val quantity: Int = 0,
    val type: ItemType = ItemType.UNKNOWN
)

/**
 * Enum representing the different types of items in the game
 */
enum class ItemType {
    GEAR,
    SEED,
    EGG,
    WEATHER,
    UNKNOWN
} 