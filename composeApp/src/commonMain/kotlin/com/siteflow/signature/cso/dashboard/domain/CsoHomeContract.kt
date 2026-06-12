package com.siteflow.signature.cso.dashboard.domain

import com.siteflow.signature.cso.onboarding.data.dto.SlabDto
import com.siteflow.signature.outlet.dashboard.data.DashboardData

/**
 * MVI Contract for the ASE Home Dashboard.
 */

sealed interface CsoHomeAction {
    data object LoadDashboard : CsoHomeAction
    data object TotalOutletsClicked : CsoHomeAction
    data object InProgressOutletsClicked : CsoHomeAction
    data object AsmPendingOutletsClicked : CsoHomeAction
    data object PendingInvoicesClicked : CsoHomeAction
}

data class CsoHomeState(
    val isLoading: Boolean = false,
    val dashboard: DashboardData? = null,
    val slabs: List<SlabDto> = emptyList(),
    val isSlabsLoading: Boolean = false
)

sealed interface CsoHomeEvent {
    data object NavigateToOutletsAll : CsoHomeEvent
    data object NavigateToOutletsInProgress : CsoHomeEvent
    data object NavigateToOutletsAsmPending : CsoHomeEvent
    data object NavigateToInvoicesPending : CsoHomeEvent
}
