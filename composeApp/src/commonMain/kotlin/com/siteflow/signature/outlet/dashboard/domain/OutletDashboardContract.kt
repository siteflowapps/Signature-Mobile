package com.siteflow.signature.outlet.dashboard.domain

import com.siteflow.signature.cso.onboarding.data.dto.SlabDto
import com.siteflow.signature.outlet.dashboard.data.OutletMonthlyPerformance
import com.siteflow.signature.outlet.dashboard.data.OutletNotification
import com.siteflow.signature.outlet.dashboard.data.OutletPayoutSummary
import com.siteflow.signature.outlet.dashboard.data.OutletRecentInvoice
import com.siteflow.signature.outlet.dashboard.data.OutletTier

/**
 * MVI Contract for the Outlet Dashboard.
 */

sealed interface OutletDashboardAction {
    data object LoadDashboard : OutletDashboardAction
    data object UploadInvoiceClicked : OutletDashboardAction
    data object ViewAllInvoicesClicked : OutletDashboardAction
    data object TotalInvoicesClicked : OutletDashboardAction
    data object PendingInvoicesClicked : OutletDashboardAction
    data class DismissNotification(val id: String) : OutletDashboardAction
    data class ViewNotification(val id: String, val notificationType: String = "") : OutletDashboardAction
    data class RecentInvoiceTapped(val invoiceId: String, val invoiceStatus: String = "") : OutletDashboardAction
}

data class OutletDashboardState(
    val performance: OutletMonthlyPerformance? = null,
    val payoutSummary: OutletPayoutSummary? = null,
    val outletTier: OutletTier = OutletTier.SILVER,
    val notifications: List<OutletNotification> = emptyList(),
    val recentInvoices: List<OutletRecentInvoice> = emptyList(),
    val slabs: List<SlabDto> = emptyList(),
    val isSlabsLoading: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface OutletDashboardEvent {
    data object NavigateToUploadInvoice : OutletDashboardEvent
    data object NavigateToInvoiceList : OutletDashboardEvent
    data object NavigateToInvoiceListAll : OutletDashboardEvent
    data object NavigateToInvoiceListPending : OutletDashboardEvent
    data class NavigateToInvoiceDetail(val invoiceId: String) : OutletDashboardEvent
}
