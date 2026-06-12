package com.siteflow.signature.core.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * Bottom navigation bar — same pattern as SiteFlow.
 * Uses Material3 NavigationBar with popUpTo + restoreState.
 *
 * iOS fix: Uses `findStartDestination().id` for popUpTo and
 * hierarchy-based selection to avoid intermittent tab click failures.
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    tabs: List<BottomTab> = CsoBottomTabs,
    modifier: Modifier = Modifier,
    onTabSelected: (tabTitle: String, previousTabTitle: String) -> Unit = { _, _ -> }
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selectedColor = AppColors.BlueGradientStart
    val unselectedColor = Color(0xFF9CA3AF)

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.destination.route

            val tint = if (selected) selectedColor else unselectedColor

            NavigationBarItem(
                selected = selected,
                onClick = {
                    val previousTab = tabs.firstOrNull { it.destination.route == currentRoute }?.title ?: ""
                    onTabSelected(tab.title, previousTab)
                    navController.navigate(tab.destination.route) {
                        // Pop up to the graph's root to avoid stacking tabs
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = AppTypography.Caption,
                        color = tint
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

