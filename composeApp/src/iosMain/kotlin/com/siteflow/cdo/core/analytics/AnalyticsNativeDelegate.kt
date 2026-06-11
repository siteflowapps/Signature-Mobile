package com.siteflow.cdo.core.analytics

/**
 * Kotlin interface that the Swift layer implements to bridge into the
 * Firebase Analytics iOS SDK. Kotlin/Native bridges this as an ObjC protocol,
 * so any Swift class conforming to it can be passed directly.
 *
 * The Swift implementation should look like:
 *
 *   class SwiftFirebaseAnalyticsDelegate: AnalyticsNativeDelegate {
 *       func logEvent(name: String, parameters: [String: Any]) {
 *           Analytics.logEvent(name, parameters: parameters)
 *       }
 *       func setUserId(userId: String?) {
 *           Analytics.setUserID(userId)
 *       }
 *       func setUserProperty(name: String, value: String) {
 *           Analytics.setUserProperty(value, forName: name)
 *       }
 *       func resetAnalyticsData() {
 *           Analytics.resetAnalyticsData()
 *       }
 *   }
 */
interface AnalyticsNativeDelegate {
    fun logEvent(name: String, parameters: Map<String, Any>)
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String)
    fun resetAnalyticsData()
}