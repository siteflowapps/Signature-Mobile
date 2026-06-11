package com.siteflow.retailsync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.siteflow.retailsync.core.navigation.AppNavHost
import com.siteflow.retailsync.core.presentation.components.loader.GlobalLoadingOverlay
import com.siteflow.retailsync.core.presentation.components.toast.GlobalToastOverlay

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(navController = navController)
            GlobalLoadingOverlay()
            GlobalToastOverlay()
        }
    }
}