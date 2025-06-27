package com.dainsleif.gaggy.model

data class VersionData(
    val version: String,
    val url: String,
    val features: List<String> = emptyList()
) 