package com.siteflow.retailsync.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

private var koinInitialized = false

fun initKoin(additionalConfig: KoinApplication.() -> Unit = {}) {
    if (koinInitialized) return
    koinInitialized = true

    startKoin {
        modules(appModules)
        additionalConfig()
    }
}
