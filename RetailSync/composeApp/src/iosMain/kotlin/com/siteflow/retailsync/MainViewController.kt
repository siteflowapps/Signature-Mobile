package com.siteflow.retailsync

import androidx.compose.ui.window.ComposeUIViewController
import com.siteflow.retailsync.di.initKoin
import com.siteflow.retailsync.di.iosPlatformModule

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin {
            modules(iosPlatformModule)
        }
    }
) {
    App()
}