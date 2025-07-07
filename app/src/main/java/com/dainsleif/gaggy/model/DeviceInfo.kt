package com.dainsleif.gaggy.model

data class DeviceInfo(
    val deviceId: String = "",
    val deviceName: String = "",
    val lastOnline: Long = 0,
    val appVersion: String = "",
    val osVersion: String = ""
) 