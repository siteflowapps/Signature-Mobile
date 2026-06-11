package com.siteflow.cdo.core.util

/**
 * Platform-agnostic logger.
 * - Android: routes to Log.d / Log.e (visible in Android Studio Logcat by tag)
 * - iOS: routes to NSLog (visible in Xcode console and Console.app)
 */
expect object CdoLog {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String)
}
