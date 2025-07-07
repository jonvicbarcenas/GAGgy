package com.dainsleif.gaggy.model

data class GardenData(
    val datas: DataContainer = DataContainer()
)

data class DataContainer(
    val eggs: EggData? = null,
    val stocks: StocksData? = null,
    val weather: WeatherData? = null
)

data class EggData(
    val items: List<ItemData> = emptyList(),
    val timestamp: Long = 0,
    val updatedAt: Long = 0
)

data class StocksData(
    val gear: CategoryData? = null,
    val seeds: CategoryData? = null
)

data class CategoryData(
    val items: List<ItemData> = emptyList(),
    val timestamp: Long = 0,
    val updatedAt: Long = 0
)

data class WeatherData(
    val items: List<WeatherItem> = emptyList(),
    val timestamp: Long = 0,
    val updatedAt: Long = 0
)

data class WeatherItem(
    val name: String = "",
    val value: String = ""
)

data class ItemData(
    val name: String = "",
    val quantity: Int = 0
) 