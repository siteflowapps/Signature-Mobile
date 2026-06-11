package com.siteflow.cdo.core.analytics

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
    fun identify(userId: String)
    fun setUserProperty(key: String, value: String)
    fun reset()
}