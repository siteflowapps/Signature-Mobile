package com.siteflow.signature

import android.app.Application
import com.siteflow.signature.di.androidPlatformModule
import com.siteflow.signature.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SiteFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidPlatform.init(this)
        startKoin {
            androidContext(this@SiteFlowApp)
            modules(
                appModules +
                        androidPlatformModule
            )
        }
    }
}
