package com.siteflow.signature

interface Platform {
    val name: String
    val appVersion: String
    val clientType: String
    val osVersion: String
    val deviceModel: String
}

expect fun getPlatform(): Platform

expect val buildEnvironment: String