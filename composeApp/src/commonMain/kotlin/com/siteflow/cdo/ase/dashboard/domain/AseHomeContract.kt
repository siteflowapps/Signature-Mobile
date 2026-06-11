package com.siteflow.cdo.ase.dashboard.domain

import com.siteflow.cdo.ase.onboarding.data.dto.SlabDto
import com.siteflow.cdo.outlet.dashboard.data.DashboardData

/**
 * MVI Contract for the ASE Home Dashboard.
 */

sealed interface AseHomeAction {
    data object LoadDashboard : AseHomeAction
    data object TotalOutletsClicked : AseHomeAction
    data object InProgressOutletsClicked : AseHomeAction
    data object AsmPendingOutletsClicked : AseHomeAction
    data object PendingInvoicesClicked : AseHomeAction
}

data class AseHomeState(
    val isLoading: Boolean = false,
    val dashboard: DashboardData? = null,
    val slabs: List<SlabDto> = emptyList(),
    val isSlabsLoading: Boolean = false
)

sealed interface AseHomeEvent {
    data object NavigateToOutletsAll : AseHomeEvent
    data object NavigateToOutletsInProgress : AseHomeEvent
    data object NavigateToOutletsAsmPending : AseHomeEvent
    data object NavigateToInvoicesPending : AseHomeEvent
}
