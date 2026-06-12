package com.siteflow.signature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.svg.SvgDecoder
import com.siteflow.signature.core.domain.ConnectivityObserver
import com.siteflow.signature.core.navigation.AppNavHost
import com.siteflow.signature.core.presentation.components.connectivity.ConnectivityState
import com.siteflow.signature.core.presentation.components.connectivity.NoInternetBanner
import com.siteflow.signature.core.presentation.components.loader.GlobalLoadingOverlay
import com.siteflow.signature.core.presentation.components.toast.GlobalToastOverlay
import org.koin.compose.koinInject

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    // Start connectivity monitoring
    val connectivityObserver: ConnectivityObserver = koinInject()
    LaunchedEffect(Unit) {
        connectivityObserver.observe().collect { status ->
            ConnectivityState.update(status)
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Connectivity banner at the very top
            NoInternetBanner()

            // Main content
            Box(modifier = Modifier.weight(1f)) {
                AppNavHost()
                GlobalLoadingOverlay()
                GlobalToastOverlay()
            }
        }
    }
}