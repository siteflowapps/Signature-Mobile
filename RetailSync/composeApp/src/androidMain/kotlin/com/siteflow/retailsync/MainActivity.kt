package com.siteflow.retailsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.siteflow.retailsync.di.androidPlatformModule
import com.siteflow.retailsync.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initKoin {
            androidContext(applicationContext)
            modules(androidPlatformModule)
        }

        setContent {
            App()
        }
    }
}