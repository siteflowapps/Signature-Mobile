// commonMain
package com.siteflow.signature.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

object KoinInitializer {

    // ✅ For Swift
    fun start() {
        startKoin {
            modules(appModules)
        }
    }

    // ✅ For Android / other platforms
    fun start(
        platformModules: List<Module>
    ) {
        startKoin {
            modules(appModules + platformModules)
        }
    }
}
