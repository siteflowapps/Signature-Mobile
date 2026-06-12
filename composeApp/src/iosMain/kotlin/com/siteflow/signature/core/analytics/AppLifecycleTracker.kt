package com.siteflow.signature.core.analytics

import com.siteflow.signature.core.domain.AuthRepository
import com.siteflow.signature.core.domain.RoleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification

class AppLifecycleTracker : KoinComponent {

    private val analytics: AnalyticsTracker by inject()
    private val authRepository: AuthRepository by inject()
    private val deviceInfo: DeviceInfo by inject()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Any? holds the opaque observer token returned by NSNotificationCenter
    private var foregroundObserver: Any? = null
    private var backgroundObserver: Any? = null

    fun trackAppOpened(isFreshInstall: Boolean) {
        analytics.track(
            AnalyticsEvent.GlobalEvent.AppOpened(
                platform = deviceInfo.platform,
                osVersion = deviceInfo.osVersion,
                appVersion = deviceInfo.appVersion,
                buildNumber = deviceInfo.buildNumber,
                isFreshInstall = isFreshInstall
            )
        )
    }

    fun start() {
        foregroundObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null,
            usingBlock = { _ -> onForeground() }
        )
        backgroundObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null,
            usingBlock = { _ -> onBackground() }
        )
    }

    private fun onForeground() {
        scope.launch {
            val userId = authRepository.getBusinessId() ?: ""
            val userRole = RoleManager.currentRole.value?.name ?: ""
            analytics.track(
                AnalyticsEvent.GlobalEvent.SessionStarted(
                    userRole = userRole,
                    userId = userId,
                    businessId = userId,
                    isReturningUser = userId.isNotBlank()
                )
            )
        }
    }

    private fun onBackground() {
        analytics.track(AnalyticsEvent.GlobalEvent.AppBackgrounded)
        analytics.track(AnalyticsEvent.GlobalEvent.SessionEnded)
    }
}