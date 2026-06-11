package com.siteflow.cdo.core.analytics

data class DeviceInfo(
    val appVersion: String,
    val buildNumber: String,
    val platform: String,
    val osVersion: String,
    val deviceModel: String,
)
