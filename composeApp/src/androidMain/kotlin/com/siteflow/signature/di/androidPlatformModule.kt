package com.siteflow.signature.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.analytics.DeviceInfo
import com.siteflow.signature.core.analytics.FirebaseAnalyticsTracker
import com.siteflow.signature.core.data.auth.AndroidTokenStorage
import com.siteflow.signature.core.data.auth.TokenStorage
import com.siteflow.signature.core.data.networking.AndroidHttpClient
import com.siteflow.signature.core.data.networking.client.HttpClientProvider
import com.siteflow.signature.core.domain.InvoicePreprocessor
import com.siteflow.signature.core.domain.LocationService
import com.siteflow.signature.core.domain.OcrService
import com.siteflow.signature.core.domain.PdfCompressor
import com.siteflow.signature.core.domain.ConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {

    single { ConnectivityObserver() }

    // 🔑 THIS IS THE MISSING PIECE
    single<HttpClientProvider> { AndroidHttpClient() }

    single {
        LocationService(
            context = androidContext(),
            permissionHelper = get()
        )
    }

    single<TokenStorage> {
        AndroidTokenStorage(androidContext())
    }

    single { OcrService(androidContext()) }

    // Invoice image preprocessing pipeline
    single { InvoicePreprocessor(androidContext()) }

    // PDF rasterize-and-rebuild compression pipeline
    single { PdfCompressor(androidContext()) }

    // General image compression and GPS watermarking pipeline
    single { com.siteflow.signature.core.domain.ImageProcessor(androidContext()) }

    single<AnalyticsTracker> {
        FirebaseAnalyticsTracker(FirebaseAnalytics.getInstance(androidContext()))
    }

    single {
        val ctx = androidContext()
        val appVersion = try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown"
        } catch (_: Exception) { "unknown" }
        val buildNumber = try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).longVersionCode.toString()
        } catch (_: Exception) { "0" }
        DeviceInfo(
            appVersion = appVersion,
            buildNumber = buildNumber,
            platform = "android",
            osVersion = android.os.Build.VERSION.RELEASE,
            deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
        )
    }
}
