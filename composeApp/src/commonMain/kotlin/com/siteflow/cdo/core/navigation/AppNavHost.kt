package com.siteflow.cdo.core.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.siteflow.cdo.ase.compliance.presentation.ComplianceScreen
import com.siteflow.cdo.ase.dashboard.presentation.AseHomeScreen
import com.siteflow.cdo.ase.dashboard.presentation.AsmHomeScreen
import com.siteflow.cdo.ase.dashboard.presentation.AseDashboardScreen
import com.siteflow.cdo.ase.dashboard.presentation.AseDashboardTopAppBar
import com.siteflow.cdo.ase.dashboard.presentation.OutletDetailScreen
import com.siteflow.cdo.ase.invoices.presentation.InvoiceDetailScreen
import com.siteflow.cdo.ase.invoices.presentation.InvoiceDetailTopAppBar
import com.siteflow.cdo.ase.invoices.presentation.InvoiceListScreen
import com.siteflow.cdo.ase.dashboard.presentation.OutletDetailTopAppBar
import com.siteflow.cdo.ase.onboarding.presentation.OnboardingStep1Screen
import com.siteflow.cdo.ase.onboarding.presentation.OnboardingStep2Screen
import com.siteflow.cdo.ase.onboarding.presentation.OnboardingStep3Screen
import com.siteflow.cdo.ase.onboarding.presentation.OnboardingStep4KycScreen
import com.siteflow.cdo.ase.onboarding.presentation.OnboardingStep4Screen
import com.siteflow.cdo.ase.onboarding.presentation.OnboardingStep5Screen
import com.siteflow.cdo.ase.onboarding.presentation.OnboardingTopAppBar
import com.siteflow.cdo.ase.profile.presentation.MyAsesScreen
import com.siteflow.cdo.ase.profile.presentation.ProfileScreen
import com.siteflow.cdo.core.analytics.AnalyticsEvent
import com.siteflow.cdo.core.analytics.AnalyticsTracker
import com.siteflow.cdo.core.analytics.ScreenStateTracker
import com.siteflow.cdo.core.domain.AppStartViewModel
import com.siteflow.cdo.core.domain.SessionEvent
import com.siteflow.cdo.core.domain.SessionManager
import com.siteflow.cdo.core.domain.RoleManager
import com.siteflow.cdo.core.domain.UserRole
import com.siteflow.cdo.core.presentation.DevRolePickerScreen
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.login.presentation.LoginScreen
import com.siteflow.cdo.login.presentation.OtpScreen
import com.siteflow.cdo.login.presentation.OtpTopAppBar
import com.siteflow.cdo.ase.onboarding.domain.OnboardingViewModel
import com.siteflow.cdo.ase.onboarding.domain.OnboardingAction
import kotlinx.coroutines.flow.collectLatest
import com.siteflow.cdo.asm.dashboard.presentation.AsmDashboardScreen
import com.siteflow.cdo.asm.dashboard.presentation.AsmOutletReviewScreen
import com.siteflow.cdo.asm.invoices.presentation.AsmInvoiceDetailScreen
import com.siteflow.cdo.asm.invoices.presentation.AsmInvoiceListScreen
import com.siteflow.cdo.outlet.dashboard.presentation.OutletDashboardScreen
import com.siteflow.cdo.outlet.walkthrough.presentation.WalkthroughAgreementScreen
import com.siteflow.cdo.outlet.walkthrough.presentation.WalkthroughOutletDetailsScreen
import com.siteflow.cdo.outlet.walkthrough.presentation.WalkthroughPaymentSummaryScreen
import com.siteflow.cdo.outlet.walkthrough.presentation.WalkthroughWelcomeScreen
import com.siteflow.cdo.outlet.invoices.presentation.OutletInvoiceDetailScreen
import com.siteflow.cdo.outlet.invoices.presentation.OutletInvoiceListScreen
import com.siteflow.cdo.support.presentation.HelpSupportScreen
import com.siteflow.cdo.support.presentation.RaiseTicketScreen
import com.siteflow.cdo.outlet.invoices.presentation.InvoiceCropReviewScreen
import com.siteflow.cdo.outlet.invoices.presentation.InvoiceProcessingScreen
import com.siteflow.cdo.outlet.invoices.presentation.InvoiceReviewScreen
import com.siteflow.cdo.outlet.invoices.presentation.PdfPreviewScreen
import com.siteflow.cdo.outlet.invoices.presentation.RegionScanScreen
import com.siteflow.cdo.outlet.invoices.presentation.UploadInvoiceScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: AppStartViewModel = koinInject()
) {
    val appState by viewModel.state.collectAsState()
    val startDestination = appState.startDestination

    LaunchedEffect(Unit) {
        viewModel.onAction(com.siteflow.cdo.core.domain.AppStartAction.ResolveStartDestination)
    }

    if (startDestination == null) return

    val navController = rememberNavController()
    val onboardingViewModel = koinInject<OnboardingViewModel>()
    val analytics = koinInject<AnalyticsTracker>()

    // 🔐 Global 401 handler
    LaunchedEffect(Unit) {
        SessionManager.events.collectLatest { event ->
            when (event) {
                SessionEvent.SessionExpired -> {
                    analytics.track(AnalyticsEvent.GlobalEvent.SessionExpired(
                        screenOnExpiry = ScreenStateTracker.screenName(navController.currentBackStackEntry?.destination?.route)
                    ))
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    /* ---------------- SCREEN TRACKING ---------------- */
    val previousRouteRef = remember { mutableStateOf<String?>(null) }
    val currentRole = RoleManager.currentRole.collectAsState().value

    LaunchedEffect(currentRoute) {
        if (currentRoute != null) {
            analytics.track(
                AnalyticsEvent.GlobalEvent.ScreenViewed(
                    screenName = ScreenStateTracker.screenName(currentRoute),
                    previousScreen = ScreenStateTracker.screenName(previousRouteRef.value),
                    userRole = currentRole?.name ?: "UNKNOWN"
                )
            )
            // Fire specific screen/funnel-start events for key destinations
            when (currentRoute) {
                AppDestination.Login.route ->
                    analytics.track(AnalyticsEvent.GlobalEvent.LoginScreenViewed(isAutoRedirect = false))
                AppDestination.WalkthroughWelcome.route ->
                    analytics.track(AnalyticsEvent.OutletEvent.WalkthroughStarted(isFirstTime = true))
                AppDestination.OutletUploadInvoice.route ->
                    analytics.track(AnalyticsEvent.OutletEvent.InvoiceUploadStarted(source = ""))
                AppDestination.OutletInvoiceCropReview.route ->
                    analytics.track(AnalyticsEvent.OutletEvent.InvoiceCropReviewViewed)
            }
            previousRouteRef.value = currentRoute
        }
    }

    /* ---------------- SYSTEM BACK TRACKING ---------------- */
    val canNavigateBack = remember(backStackEntry) {
        navController.previousBackStackEntry != null
    }
    PlatformBackHandler(enabled = canNavigateBack) {
        analytics.track(AnalyticsEvent.GlobalEvent.BackButtonTapped(
            screenName = ScreenStateTracker.screenName(currentRoute)
        ))
        navController.navigateUp()
    }

    /* ---------------- VISIBILITY RULES ---------------- */

    val walkthroughRoutes = setOf(
        AppDestination.WalkthroughWelcome.route,
        AppDestination.WalkthroughOutletDetails.route,
        AppDestination.WalkthroughAgreement.route,
        AppDestination.WalkthroughPayment.route
    )

    val showTopBar = when (currentRoute) {
        AppDestination.Login.route,
        AppDestination.RoleSelect.route,
        AppDestination.OutletInvoiceProcessing.route,
        AppDestination.OutletInvoiceCropReview.route -> false // immersive full-screen
        else -> currentRoute !in walkthroughRoutes
    }

    val dashboardRoutes = setOf(
        AppDestination.AseHome.route,
        AppDestination.AseDashboard.route,
        AppDestination.AseInvoices.route,
        AppDestination.OutletDashboard.route,
        AppDestination.OutletInvoices.route,
        AppDestination.AsmHome.route,
        AppDestination.AsmDashboard.route,
        AppDestination.AsmInvoices.route,
        AppDestination.Profile.route
    )
    val showBottomBar = currentRoute in dashboardRoutes && currentRoute !in walkthroughRoutes
    
    val authRepo = koinInject<com.siteflow.cdo.core.domain.AuthRepository>()
    val userNameState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    LaunchedEffect(currentRoute) {
        userNameState.value = authRepo.getUserName() ?: ""
    }
    val userName = userNameState.value.trim().split("\\s+".toRegex()).firstOrNull() ?: ""
    val bottomTabs = when (currentRole) {
        UserRole.ASM -> AsmBottomTabs
        UserRole.OUTLET -> OutletBottomTabs
        else -> AseBottomTabs
    }

    AppScaffold(
        title = null,
        showTopBar = showTopBar,
        showBottomBar = showBottomBar,

        /* ---------------- TOP BAR ---------------- */
        topBar = {
            when (currentRoute) {
                AppDestination.AseHome.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                AppDestination.AseDashboard.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                AppDestination.AseInvoices.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                AppDestination.OutletDashboard.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                AppDestination.OutletInvoices.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                AppDestination.OutletInvoiceDetail.route -> InvoiceDetailTopAppBar(
                    title = "Invoice Details",
                    onBack = { navController.popBackStack() }
                )
                AppDestination.OutletUploadInvoice.route -> InvoiceDetailTopAppBar(
                    title = "Upload Invoice",
                    onBack = { navController.popBackStack() }
                )
                AppDestination.OutletInvoiceReview.route -> InvoiceDetailTopAppBar(
                    title = "Review Invoice",
                    onBack = { navController.popBackStack() }
                )
                AppDestination.OutletInvoicePdfPreview.route -> InvoiceDetailTopAppBar(
                    title = "Review PDF",
                    onBack = { navController.popBackStack() }
                )
                // CropReview has no top bar (showTopBar = false)

                AppDestination.AseOutletDetails.route -> OutletDetailTopAppBar(
                    title = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<String>("outletName") ?: "Outlet Details",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseCompliance.route -> OutletDetailTopAppBar(
                    title = "Submit Compliance",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AsmHome.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                AppDestination.AsmDashboard.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                AppDestination.AsmInvoices.route -> AseDashboardTopAppBar(
                        userName = userName,
                        onProfileClick = {
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                AppDestination.AsmOutletReview.route -> OutletDetailTopAppBar(
                    title = "Review Outlet",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseInvoiceDetail.route -> InvoiceDetailTopAppBar(
                    title = "Invoice Review",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AsmInvoiceDetail.route -> InvoiceDetailTopAppBar(
                    title = "Invoice Review",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AsmMyTeam.route -> InvoiceDetailTopAppBar(
                    title = "My Team",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.RaiseTicket.route -> InvoiceDetailTopAppBar(
                    title = "Raise an Issue",
                    onBack = { navController.popBackStack() }
                )


                AppDestination.VerifyOtp.route -> OtpTopAppBar(
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseOnboardOutlet.route -> OnboardingTopAppBar(
                    currentStep = 1,
                    totalSteps = 6,
                    stepLabel = "Basic Details",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseOnboardStep2.route -> OnboardingTopAppBar(
                    currentStep = 2,
                    totalSteps = 6,
                    stepLabel = "Classification & Volume",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseOnboardStep3.route -> OnboardingTopAppBar(
                    currentStep = 3,
                    totalSteps = 6,
                    stepLabel = "Distributor & Bank",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseOnboardStep4.route -> OnboardingTopAppBar(
                    currentStep = 4,
                    totalSteps = 6,
                    stepLabel = "KYC Details",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseOnboardStep5.route -> OnboardingTopAppBar(
                    currentStep = 5,
                    totalSteps = 6,
                    stepLabel = "Photos & Verification",
                    onBack = { navController.popBackStack() }
                )

                AppDestination.AseOnboardStep6.route -> OnboardingTopAppBar(
                    currentStep = 6,
                    totalSteps = 6,
                    stepLabel = "PFP Agreement",
                    onBack = { navController.popBackStack() }
                )
            }
        },

        /* ---------------- FAB ---------------- */
        floatingActionButton = {
            if (currentRoute == AppDestination.AseDashboard.route) {
                FloatingActionButton(
                    onClick = {
                        onboardingViewModel.onAction(OnboardingAction.ResetState)
                        navController.navigate(AppDestination.AseOnboardOutlet.route)
                    },
                    containerColor = AppColors.BlueGradientStart,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Outlet"
                    )
                }
            }
        },

        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp
                    )
                    BottomNavBar(
                        navController = navController,
                        tabs = bottomTabs,
                        onTabSelected = { tabTitle, previousTabTitle ->
                            analytics.track(AnalyticsEvent.GlobalEvent.BottomTabTapped(
                                tab = tabTitle,
                                previousTab = previousTabTitle,
                                userRole = currentRole?.name ?: ""
                            ))
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = modifier.padding(paddingValues)
        ) {
            /* ---------------- LOGIN ---------------- */
            composable(AppDestination.Login.route) {
                LoginScreen(
                    onNavigateToOtp = { mobileNumber ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mobileNumber", mobileNumber)
                        navController.navigate(AppDestination.VerifyOtp.route)
                    }
                )
            }

            /* ---------------- VERIFY OTP ---------------- */
            composable(AppDestination.VerifyOtp.route) {
                val mobileNumber = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>("mobileNumber") ?: ""

                OtpScreen(
                    mobileNumber = mobileNumber,
                    onVerified = { role ->
                        val dest = when (role) {
                            UserRole.ASE -> AppDestination.AseHome.route
                            UserRole.ASM -> AppDestination.AsmHome.route
                            UserRole.OUTLET -> AppDestination.OutletDashboard.route
                        }
                        navController.navigate(dest) {
                            popUpTo(AppDestination.Login.route) { inclusive = true }
                        }
                    },
                    onEditNumber = {
                        navController.popBackStack()
                    },
                    onNavigateToWalkthrough = {
                        navController.navigate(AppDestination.WalkthroughWelcome.route) {
                            popUpTo(AppDestination.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            /* ---------------- DEV ROLE PICKER ---------------- */
            composable(AppDestination.RoleSelect.route) {
                DevRolePickerScreen(
                    onRoleSelected = { role: UserRole ->
                        val destination = when (role) {
                            UserRole.ASE -> AppDestination.AseHome.route
                            UserRole.OUTLET -> AppDestination.OutletDashboard.route
                            UserRole.ASM -> AppDestination.AsmHome.route
                        }
                        navController.navigate(destination) {
                            popUpTo(AppDestination.RoleSelect.route) { inclusive = true }
                        }
                    }
                )
            }

            /* ---------------- ASE HOME DASHBOARD ---------------- */
            composable(AppDestination.AseHome.route) {
                AseHomeScreen(
                    onNavigateToOutlets = { filter ->
                        // Map Home event names to dashboard chip labels
                        val chipFilter = when (filter) {
                            "In Progress" -> "Draft"
                            "ASM Pending" -> "Pending"
                            else -> filter
                        }
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletFilter", chipFilter)
                        navController.navigate(AppDestination.AseDashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onNavigateToInvoices = { filter ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("invoiceFilter", filter)
                        navController.navigate(AppDestination.AseInvoices.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }

            /* ---------------- ASE OUTLETS (was Home tab) ---------------- */
            composable(AppDestination.AseDashboard.route) { backStackEntry ->
                // Read the refresh flag set when returning from onboarding
                val needsRefresh = backStackEntry.savedStateHandle
                    .get<Boolean>("needsRefresh") ?: false

                // Read and consume the filter set from Home dashboard (one-time)
                val initialFilter = navController.previousBackStackEntry
                    ?.savedStateHandle?.remove<String>("outletFilter")

                AseDashboardScreen(
                    initialFilter = initialFilter,
                    onViewDetails = { outlet ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletId", outlet.id)
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletName", outlet.name)
                        navController.navigate(AppDestination.AseOutletDetails.route)
                    },
                    onContinueOnboarding = { outletId, step ->
                        onboardingViewModel.onAction(OnboardingAction.ResetState)
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletId", outletId)
                        
                        val destination = when (step) {
                            2 -> AppDestination.AseOnboardStep2.route
                            3 -> AppDestination.AseOnboardStep3.route
                            4 -> AppDestination.AseOnboardStep4.route
                            5 -> AppDestination.AseOnboardStep5.route
                            6 -> AppDestination.AseOnboardStep6.route
                            else -> AppDestination.AseOnboardStep2.route
                        }
                        navController.navigate(destination)
                    },
                    needsRefresh = needsRefresh,
                    onRefreshConsumed = {
                        backStackEntry.savedStateHandle["needsRefresh"] = false
                    }
                )
            }

            /* ---------------- ASE OUTLET DETAILS ---------------- */
            composable(AppDestination.AseOutletDetails.route) {
                val outletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId") ?: ""
                OutletDetailScreen(
                    outletId = outletId,
                    onBack = { navController.popBackStack() },
                    onAssetRequestSuccess = {
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack(AppDestination.AseDashboard.route, inclusive = false)
                    },
                    onSubmitCompliance = {
                        analytics.track(AnalyticsEvent.ASEEvent.ComplianceTapped(outletId = outletId))
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletId", outletId)
                        navController.navigate(AppDestination.AseCompliance.route)
                    }
                )
            }

            /* ---------------- ASE COMPLIANCE ---------------- */
            composable(AppDestination.AseCompliance.route) {
                val outletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId") ?: ""
                ComplianceScreen(
                    outletId = outletId,
                    onBack = {
                        // Refresh dashboard and pop all the way back to the outlet list
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack(AppDestination.AseDashboard.route, inclusive = false)
                    }
                )
            }

            /* ---------------- ASE ONBOARD OUTLET ---------------- */
            composable(AppDestination.AseOnboardOutlet.route) {
                OnboardingStep1Screen(
                    onBack = {
                        // Flag dashboard to refresh when we return
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack()
                    },
                    onContinue = {
                        navController.navigate(AppDestination.AseOnboardStep2.route)
                    }
                )
            }

            /* ---------------- ASE ONBOARD STEP 2 (VOLUME) ---------------- */
            composable(AppDestination.AseOnboardStep2.route) {
                val resumedOutletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId")
                OnboardingStep2Screen(
                    outletId = resumedOutletId,
                    onBack = {
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack()
                    },
                    onContinue = {
                        // Forward outletId to next step
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletId", resumedOutletId)
                        navController.navigate(AppDestination.AseOnboardStep3.route)
                    }
                )
            }

            /* ---------------- ASE ONBOARD STEP 3 (DISTRIBUTOR & BANK) ---------------- */
            composable(AppDestination.AseOnboardStep3.route) {
                val resumedOutletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId")
                OnboardingStep4Screen(
                    onBack = {
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack()
                    },
                    onContinue = {
                        // Forward outletId to next step
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletId", resumedOutletId)
                        navController.navigate(AppDestination.AseOnboardStep4.route)
                    },
                    outletId = resumedOutletId
                )
            }

            /* ---------------- ASE ONBOARD STEP 4 (KYC) ---------------- */
            composable(AppDestination.AseOnboardStep4.route) {
                val resumedOutletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId")
                OnboardingStep4KycScreen(
                    onBack = {
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack()
                    },
                    onContinue = {
                        // Forward outletId to next step
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletId", resumedOutletId)
                        navController.navigate(AppDestination.AseOnboardStep5.route)
                    },
                    outletId = resumedOutletId
                )
            }

            /* ---------------- ASE ONBOARD STEP 5 (PHOTOS) ---------------- */
            composable(AppDestination.AseOnboardStep5.route) {
                val resumedOutletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId")
                OnboardingStep3Screen(
                    onBack = {
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack()
                    },
                    onContinue = {
                        // Forward outletId to next step
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletId", resumedOutletId)
                        navController.navigate(AppDestination.AseOnboardStep6.route)
                    },
                    outletId = resumedOutletId
                )
            }

            /* ---------------- ASE ONBOARD STEP 6 (AGREEMENT) ---------------- */
            composable(AppDestination.AseOnboardStep6.route) {
                val resumedOutletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId")
                OnboardingStep5Screen(
                    onBack = {
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        navController.popBackStack()
                    },
                    onComplete = {
                        // Reset onboarding state so next outlet starts fresh
                        onboardingViewModel.onAction(OnboardingAction.ResetState)
                        // Set refresh flag on the dashboard entry before popping back
                        navController.getBackStackEntry(AppDestination.AseDashboard.route)
                            .savedStateHandle["needsRefresh"] = true
                        // Pop all onboarding screens, returning to the existing dashboard
                        navController.popBackStack(
                            AppDestination.AseDashboard.route,
                            inclusive = false
                        )
                    },
                    outletId = resumedOutletId
                )
            }

            /* ---------------- ASE INVOICES ---------------- */
            composable(AppDestination.AseInvoices.route) {
                val initialFilter = navController.previousBackStackEntry
                    ?.savedStateHandle?.remove<String>("invoiceFilter")

                InvoiceListScreen(
                    initialFilter = initialFilter,
                    onViewInvoiceDetails = { invoiceId ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("invoiceId", invoiceId)
                        navController.navigate(AppDestination.AseInvoiceDetail.route)
                    }
                )
            }

            /* ---------------- ASE INVOICE DETAIL ---------------- */
            composable(AppDestination.AseInvoiceDetail.route) {
                val invoiceId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("invoiceId") ?: ""
                InvoiceDetailScreen(
                    invoiceId = invoiceId,
                    onBack = { navController.popBackStack() }
                )
            }

            /* ---------------- OUTLET DASHBOARD ---------------- */
            composable(AppDestination.OutletDashboard.route) {
                val uploadVm = koinInject<com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceViewModel>()
                OutletDashboardScreen(
                    onUploadInvoice = {
                        // Reset form state before entering the upload flow
                        uploadVm.onAction(com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceAction.ResetState)
                        navController.navigate(AppDestination.OutletUploadInvoice.route)
                    },
                    onViewAllInvoices = {
                        navController.navigate(AppDestination.OutletInvoices.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToInvoices = { filter ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("initialFilter", filter)
                        navController.navigate(AppDestination.OutletInvoices.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false  // Don't restore — we want the new filter
                        }
                    }
                )
            }

            /* ---------------- OUTLET INVOICES ---------------- */
            composable(AppDestination.OutletInvoices.route) {
                val initialFilter = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("initialFilter")
                OutletInvoiceListScreen(
                    onViewInvoiceDetails = { invoiceId ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("invoiceId", invoiceId)
                        navController.navigate(AppDestination.OutletInvoiceDetail.route)
                    },
                    initialFilter = initialFilter
                )
            }

            /* ---------------- OUTLET INVOICE DETAIL ---------------- */
            composable(AppDestination.OutletInvoiceDetail.route) {
                val invoiceId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("invoiceId") ?: ""
                OutletInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    onBack = { navController.popBackStack() }
                )
            }

            /* ---------------- OUTLET UPLOAD INVOICE ---------------- */
            composable(AppDestination.OutletUploadInvoice.route) {
                UploadInvoiceScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToProcessing = {
                        navController.navigate(AppDestination.OutletInvoiceProcessing.route)
                    },
                    onNavigateToCropReview = { imagePath ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("cropImagePath", imagePath)
                        navController.navigate(AppDestination.OutletInvoiceCropReview.route)
                    },
                    onNavigateToPdfPreview = {
                        navController.navigate(AppDestination.OutletInvoicePdfPreview.route)
                    },
                    onScanRegion = { itemIndex, field ->
                        navController.navigate(
                            AppDestination.OutletRegionScan.route(itemIndex, field.name)
                        )
                    }
                )
            }

            /* ---------------- OUTLET INVOICE PDF PREVIEW ---------------- */
            composable(AppDestination.OutletInvoicePdfPreview.route) {
                PdfPreviewScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToProcessing = {
                        navController.navigate(AppDestination.OutletInvoiceProcessing.route) {
                            // Remove the preview from the back stack so back goes to upload-source picker.
                            popUpTo(AppDestination.OutletInvoicePdfPreview.route) { inclusive = true }
                        }
                    },
                )
            }

            /* ---------------- OUTLET INVOICE CROP REVIEW ---------------- */
            composable(AppDestination.OutletInvoiceCropReview.route) {
                val imagePath = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("cropImagePath") ?: ""
                val uploadVm = koinInject<com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceViewModel>()
                val imagePicker = koinInject<com.siteflow.cdo.core.domain.ImagePicker>()
                InvoiceCropReviewScreen(
                    imagePath = imagePath,
                    onConfirm = {
                        // Pass confirmed image back and process it
                        analytics.track(AnalyticsEvent.OutletEvent.InvoiceCropConfirmed)
                        uploadVm.onAction(
                            com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceAction.ImageCaptured(imagePath)
                        )
                        navController.popBackStack()
                    },
                    onRetake = {
                        analytics.track(AnalyticsEvent.OutletEvent.InvoiceCropRetake)
                        navController.popBackStack()
                        imagePicker.openDocumentScanner(
                            onImagePicked = { path ->
                                navController.currentBackStackEntry
                                    ?.savedStateHandle?.set("cropImagePath", path)
                                navController.navigate(AppDestination.OutletInvoiceCropReview.route)
                            },
                            onPermissionDenied = { /* handled by system */ }
                        )
                    }
                )
            }

            /* ---------------- OUTLET INVOICE PROCESSING (AI) ---------------- */
            composable(AppDestination.OutletInvoiceProcessing.route) {
                InvoiceProcessingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToReview = {
                        navController.navigate(AppDestination.OutletInvoiceReview.route) {
                            // Remove processing screen from back stack so back goes to upload
                            popUpTo(AppDestination.OutletInvoiceProcessing.route) { inclusive = true }
                        }
                    }
                )
            }

            /* ---------------- OUTLET INVOICE REVIEW ---------------- */
            composable(AppDestination.OutletInvoiceReview.route) {
                InvoiceReviewScreen(
                    onSubmitSuccess = {
                        navController.popBackStack(
                            AppDestination.OutletDashboard.route,
                            inclusive = false
                        )
                    }
                )
            }

            /* ------------- OUTLET REGION SCAN (manual OCR field) ------------- */
            composable(
                route = AppDestination.OutletRegionScan.route,
                arguments = listOf(
                    androidx.navigation.navArgument("itemIndex") { type = androidx.navigation.NavType.IntType },
                    androidx.navigation.navArgument("fieldName") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val itemIndex = backStackEntry.savedStateHandle.get<Int>("itemIndex") ?: 0
                val fieldName = backStackEntry.savedStateHandle.get<String>("fieldName") ?: ""
                val field = com.siteflow.cdo.outlet.invoices.domain.LineItemField.entries
                    .firstOrNull { it.name == fieldName }
                    ?: com.siteflow.cdo.outlet.invoices.domain.LineItemField.QUANTITY
                val uploadVm = koinInject<com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceViewModel>()
                val uploadState by uploadVm.state.collectAsState()
                val imagePath = uploadState.capturedImagePath ?: ""
                LaunchedEffect(itemIndex, fieldName) {
                    analytics.track(AnalyticsEvent.OutletEvent.InvoiceRegionScanUsed(
                        itemIndex = itemIndex,
                        fieldName = fieldName
                    ))
                }
                RegionScanScreen(
                    imagePath = imagePath,
                    fieldLabel = fieldName.lowercase().replace('_', ' '),
                    onResult = { text ->
                        uploadVm.onAction(
                            com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceAction.UpdateLineItem(
                                index = itemIndex,
                                field = field,
                                value = text
                            )
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            /* ---------------- WALKTHROUGH (RETAILER FIRST-TIME) ---------------- */
            composable(AppDestination.WalkthroughWelcome.route) {
                WalkthroughWelcomeScreen(
                    onNavigateToOutletDetails = {
                        navController.navigate(AppDestination.WalkthroughOutletDetails.route)
                    }
                )
            }


            composable(AppDestination.WalkthroughOutletDetails.route) {
                WalkthroughOutletDetailsScreen(
                    onNavigateToAgreement = {
                        navController.navigate(AppDestination.WalkthroughAgreement.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.WalkthroughAgreement.route) {
                WalkthroughAgreementScreen(
                    onNavigateToPayment = {
                        navController.navigate(AppDestination.WalkthroughPayment.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.WalkthroughPayment.route) {
                WalkthroughPaymentSummaryScreen(
                    onNavigateToDashboard = {
                        navController.navigate(AppDestination.OutletDashboard.route) {
                            popUpTo(AppDestination.WalkthroughWelcome.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            /* ---------------- ASM SCREENS ---------------- */
            composable(AppDestination.AsmHome.route) {
                AsmHomeScreen(
                    onNavigateToOutlets = { filter ->
                        // Map Home event names to ASM dashboard chip labels
                        val chipFilter = when (filter) {
                            "Active" -> "Active"
                            "In Progress" -> "In Progress"
                            "Suspended" -> "Suspended"
                            "ASM Pending" -> "Pending Review"
                            else -> filter
                        }
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("outletFilter", chipFilter)
                        navController.navigate(AppDestination.AsmDashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onNavigateToInvoices = { filter ->
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("invoiceFilter", filter)
                        navController.navigate(AppDestination.AsmInvoices.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onNavigateToMyAses = {
                        navController.navigate(AppDestination.AsmMyTeam.route)
                    }
                )
            }

            composable(AppDestination.AsmDashboard.route) {
                // Read and consume the filter set from Home dashboard (one-time)
                val initialFilter = navController.previousBackStackEntry
                    ?.savedStateHandle?.remove<String>("outletFilter")

                AsmDashboardScreen(
                    initialFilter = initialFilter,
                    onReviewOutlet = { outlet ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("outletId", outlet.id)
                        navController.currentBackStackEntry?.savedStateHandle?.set("outletName", outlet.name)
                        navController.navigate(AppDestination.AsmOutletReview.route)
                    },
                    onViewDetails = { outlet ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("outletId", outlet.id)
                        navController.currentBackStackEntry?.savedStateHandle?.set("outletName", outlet.name)
                        navController.navigate(AppDestination.AsmOutletReview.route)
                    }
                )
            }

            composable(AppDestination.AsmOutletReview.route) {
                val outletId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("outletId") ?: ""
                AsmOutletReviewScreen(
                    outletId = outletId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.AsmInvoices.route) {
                val initialFilter = navController.previousBackStackEntry
                    ?.savedStateHandle?.remove<String>("invoiceFilter")

                AsmInvoiceListScreen(
                    initialFilter = initialFilter,
                    onViewInvoiceDetails = { invoiceId ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("invoiceId", invoiceId)
                        navController.navigate(AppDestination.AsmInvoiceDetail.route)
                    }
                )
            }

            composable(AppDestination.AsmInvoiceDetail.route) {
                val invoiceId = navController.previousBackStackEntry
                    ?.savedStateHandle?.get<String>("invoiceId") ?: ""
                AsmInvoiceDetailScreen(
                    invoiceId = invoiceId,
                    onBack = { navController.popBackStack() }
                )
            }

            /* ---------------- PROFILE ---------------- */
            composable(AppDestination.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToMyTeam = {
                        navController.navigate(AppDestination.AsmMyTeam.route)
                    },
                    onHelpSupport = {
                        navController.navigate(AppDestination.HelpSupport.route)
                    }
                )
            }

            /* ---------------- HELP & SUPPORT ---------------- */
            composable(AppDestination.HelpSupport.route) {
                HelpSupportScreen(
                    onBack = { navController.popBackStack() },
                    onRaiseTicket = {
                        navController.navigate(AppDestination.RaiseTicket.route)
                    }
                )
            }

            /* ---------------- RAISE TICKET ---------------- */
            composable(AppDestination.RaiseTicket.route) {
                RaiseTicketScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.AsmMyTeam.route) {
                MyAsesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
