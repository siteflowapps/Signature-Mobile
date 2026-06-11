package com.siteflow.cdo.core.analytics

class FirebaseAnalyticsTracker(
    private val delegate: AnalyticsNativeDelegate
) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        delegate.logEvent(
            name = event.toEventName(),
            parameters = event.toEventParams()
        )
    }

    override fun identify(userId: String) {
        delegate.setUserId(userId)
    }

    override fun setUserProperty(key: String, value: String) {
        delegate.setUserProperty(name = key, value = value)
    }

    override fun reset() {
        delegate.resetAnalyticsData()
    }
}