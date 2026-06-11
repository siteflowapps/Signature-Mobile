package com.siteflow.cdo.di

import com.siteflow.cdo.core.analytics.DeviceInfo
import com.siteflow.cdo.core.data.auth.IosTokenStorage
import com.siteflow.cdo.core.data.auth.TokenStorage
import com.siteflow.cdo.core.data.networking.IosHttpClient
import com.siteflow.cdo.core.data.networking.client.HttpClientProvider
import com.siteflow.cdo.core.domain.ImagePicker
import com.siteflow.cdo.core.domain.InvoicePreprocessor
import com.siteflow.cdo.core.domain.LocationService
import com.siteflow.cdo.core.domain.OcrService
import com.siteflow.cdo.core.domain.PdfCompressor
import com.siteflow.cdo.core.domain.PdfPicker
import com.siteflow.cdo.core.domain.ConnectivityObserver
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
    single { com.siteflow.cdo.core.domain.ImageProcessor() }

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
