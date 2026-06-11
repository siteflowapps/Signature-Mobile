package com.siteflow.cdo.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String?,
    showTopBar: Boolean,
    showBottomBar: Boolean,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Box {

        Scaffold(
            topBar = {
                if (showTopBar) {
                    when {
                        topBar != null -> topBar()
                        title != null -> {
                            TopAppBar(
                                title = { Text(title) },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (showBottomBar) bottomBar()
            },
            floatingActionButton = {
                floatingActionButton?.invoke()
            }
        ) { padding ->
            content(padding)
        }
    }
}


