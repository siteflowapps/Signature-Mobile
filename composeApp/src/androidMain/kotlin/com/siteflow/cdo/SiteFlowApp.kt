package com.siteflow.cdo

import android.app.Application
import com.siteflow.cdo.di.androidPlatformModule
import com.siteflow.cdo.di.appModules
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
