package com.siteflow.signature.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront

data class BottomTab(
    val destination: AppDestination,
    val title: String,
    val icon: ImageVector
)

val CsoBottomTabs = listOf(
    BottomTab(
        destination = AppDestination.CsoHome,
        title = "Home",
        icon = Icons.Outlined.Home
    ),
    BottomTab(
        destination = AppDestination.CsoDashboard,
        title = "Outlets",
        icon = Icons.Outlined.Storefront
    ),
    BottomTab(
        destination = AppDestination.Profile,
        title = "Profile",
        icon = Icons.Outlined.Person
    )
)

val AseBottomTabs = listOf(
    BottomTab(
        destination = AppDestination.CsoHome,
        title = "Home",
        icon = Icons.Outlined.Home
    ),
    BottomTab(
        destination = AppDestination.CsoDashboard,
        title = "Outlets",
        icon = Icons.Outlined.Storefront
    ),
    BottomTab(
        destination = AppDestination.CsoInvoices,
        title = "Invoices",
        icon = Icons.Outlined.Receipt
    ),
    BottomTab(
        destination = AppDestination.Profile,
        title = "Profile",
        icon = Icons.Outlined.Person
    )
)

val AsmBottomTabs = listOf(
    BottomTab(
        destination = AppDestination.AsmHome,
        title = "Home",
        icon = Icons.Outlined.Home
    ),
    BottomTab(
        destination = AppDestination.AsmDashboard,
        title = "Outlets",
        icon = Icons.Outlined.Storefront
    ),
    BottomTab(
        destination = AppDestination.AsmInvoices,
        title = "Invoices",
        icon = Icons.Outlined.Receipt
    ),
    BottomTab(
        destination = AppDestination.Profile,
        title = "Profile",
        icon = Icons.Outlined.Person
    )
)

val OutletBottomTabs = listOf(
    BottomTab(
        destination = AppDestination.OutletDashboard,
        title = "Home",
        icon = Icons.Outlined.Home
    ),
    BottomTab(
        destination = AppDestination.OutletInvoices,
        title = "Invoices",
        icon = Icons.Outlined.Receipt
    ),
    BottomTab(
        destination = AppDestination.Profile,
        title = "Profile",
        icon = Icons.Outlined.Person
    )
)
