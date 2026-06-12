package com.siteflow.signature.di

import com.siteflow.signature.core.analytics.AnalyticsNativeDelegate
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.analytics.FirebaseAnalyticsTracker
import org.koin.dsl.module

/**
 * Call this from Swift and pass the result as a platform module to
 * KoinInitializer.start(platformModules:).
 *
 * Swift usage:
 *   let analyticsModule = AnalyticsModuleKt.iosAnalyticsModule(
 *       delegate: SwiftFirebaseAnalyticsDelegate()
 *   )
 *   KoinInitializer().start(platformModules: [iosPlatformModule, analyticsModule])
 */
fun iosAnalyticsModule(delegate: AnalyticsNativeDelegate) = module {
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(delegate) }
}