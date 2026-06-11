package com.siteflow.retailsync.core.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.siteflow.retailsync.core.domain.AppStartViewModel
import com.siteflow.retailsync.core.domain.SessionEvent
import com.siteflow.retailsync.core.domain.SessionManager
import com.siteflow.retailsync.invoice.presentation.InvoiceListScreen
import com.siteflow.retailsync.login.presentation.LoginScreen
import com.siteflow.retailsync.login.presentation.OtpScreen
import com.siteflow.retailsync.order.presentation.OrderCreationScreen
import com.siteflow.retailsync.order.presentation.OrderSuccessScreen
import com.siteflow.retailsync.scanner.presentation.ScannerScreen
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController,
    appStartViewModel: AppStartViewModel = koinInject()
) {
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = appStartViewModel.resolveStartDestination()
    }

    LaunchedEffect(Unit) {
        SessionManager.events.collectLatest { event ->
            when (event) {
                SessionEvent.SessionExpired -> {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    if (startDestination == null) return

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        // ── Login ──
        composable(AppDestination.Login.route) {
            LoginScreen(
                onNavigateToOtp = { mobile ->
                    navController.navigate(AppDestination.VerifyOtp.createRoute(mobile))
                }
            )
        }

        // ── OTP ──
        composable(
            route = AppDestination.VerifyOtp.route,
            arguments = listOf(navArgument("mobileNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val mobileNumber = backStackEntry.savedStateHandle.get<String>("mobileNumber")
                ?: backStackEntry.arguments?.let {
                    // KMP-compatible: parse from the route directly
                    navController.currentBackStackEntry?.destination?.route
                } ?: ""
            // For KMP, we extract from the actual navigated path
            val actualMobile = navController.currentBackStackEntry
                ?.arguments
                ?.toString()
                ?.let { argsStr ->
                    // The argument is embedded in the route
                    val entry = navController.currentBackStackEntry
                    entry?.destination?.route?.let { template ->
                        val path = entry.destination.route ?: ""
                        // Use the route segment
                        ""
                    }
                } ?: ""

            // Simple approach: extract from the route string
            val routeStr = navController.currentBackStackEntry?.destination?.route ?: ""

            OtpScreen(
                mobileNumber = mobileNumber,
                onNavigateToDashboard = {
                    navController.navigate(AppDestination.InvoiceList.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Invoice List ──
        composable(AppDestination.InvoiceList.route) {
            InvoiceListScreen(
                onNavigateToScanner = {
                    navController.navigate(AppDestination.Scanner.route)
                },
                onNavigateToLogin = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Scanner ──
        composable(AppDestination.Scanner.route) {
            ScannerScreen(
                onNavigateToOrderCreation = { outletId, outletName ->
                    navController.navigate(
                        AppDestination.OrderCreation.createRoute(outletId, outletName)
                    )
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Order Creation ──
        composable(
            route = AppDestination.OrderCreation.route,
            arguments = listOf(
                navArgument("outletId") { type = NavType.StringType },
                navArgument("outletName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val outletId = backStackEntry.savedStateHandle.get<String>("outletId") ?: ""
            val outletName = backStackEntry.savedStateHandle.get<String>("outletName") ?: ""
            OrderCreationScreen(
                outletId = outletId,
                outletName = outletName,
                onNavigateToSuccess = { orderId ->
                    navController.navigate(AppDestination.OrderSuccess.createRoute(orderId)) {
                        popUpTo(AppDestination.InvoiceList.route) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Order Success ──
        composable(
            route = AppDestination.OrderSuccess.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.savedStateHandle.get<String>("orderId") ?: ""
            OrderSuccessScreen(
                orderId = orderId,
                onNavigateToInvoices = {
                    navController.navigate(AppDestination.InvoiceList.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
