package com.siteflow.signature.core.util

/**
 * Platform-agnostic logger.
 * - Android: routes to Log.d / Log.e (visible in Android Studio Logcat by tag)
 * - iOS: routes to NSLog (visible in Xcode console and Console.app)
 */
expect object SignatureLog {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String)
}
