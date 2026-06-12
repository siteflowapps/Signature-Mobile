package com.siteflow.signature.di

import com.siteflow.signature.core.analytics.DeviceInfo
import com.siteflow.signature.core.data.auth.IosTokenStorage
import com.siteflow.signature.core.data.auth.TokenStorage
import com.siteflow.signature.core.data.networking.IosHttpClient
import com.siteflow.signature.core.data.networking.client.HttpClientProvider
import com.siteflow.signature.core.domain.ImagePicker
import com.siteflow.signature.core.domain.InvoicePreprocessor
import com.siteflow.signature.core.domain.LocationService
import com.siteflow.signature.core.domain.OcrService
import com.siteflow.signature.core.domain.PdfCompressor
import com.siteflow.signature.core.domain.PdfPicker
import com.siteflow.signature.core.domain.ConnectivityObserver
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

val iosPlatformModule = module {

    single { ConnectivityObserver() }

    // 🔑 REQUIRED for networking
    single<HttpClientProvider> { IosHttpClient() }

    // Existing bindings
    single { LocationService() }
    single { ImagePicker() }
    single { OcrService() }
    single { PdfPicker() }

    // Invoice image preprocessing pipeline
    single { InvoicePreprocessor() }

    // PDF rasterize-and-rebuild compression pipeline
    single { PdfCompressor() }

    // General image compression and GPS watermarking pipeline
    single { com.siteflow.signature.core.domain.ImageProcessor() }

    single<TokenStorage> {
        IosTokenStorage()
    }

    single {
        DeviceInfo(
            appVersion = NSBundle.mainBundle.infoDictionary
                ?.get("CFBundleShortVersionString") as? String ?: "unknown",
            buildNumber = NSBundle.mainBundle.infoDictionary
                ?.get("CFBundleVersion") as? String ?: "0",
            platform = "ios",
            osVersion = UIDevice.currentDevice.systemVersion,
            deviceModel = UIDevice.currentDevice.model
        )
    }
}
