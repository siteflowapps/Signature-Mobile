package com.siteflow.signature.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        val name = event.toEventName()
        val params = event.toEventParams()
        firebaseAnalytics.logEvent(name, params.toBundle())
    }

    override fun identify(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperty(key: String, value: String) {
        firebaseAnalytics.setUserProperty(key, value)
    }

    override fun reset() {
        firebaseAnalytics.resetAnalyticsData()
    }

    private fun Map<String, Any>.toBundle(): Bundle = Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> putString(key, value.toString())
            }
        }
    }
}