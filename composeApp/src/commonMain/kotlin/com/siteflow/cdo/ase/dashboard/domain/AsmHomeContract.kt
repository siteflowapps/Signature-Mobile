package com.siteflow.cdo.ase.dashboard.domain

import com.siteflow.cdo.ase.onboarding.data.dto.SlabDto
import com.siteflow.cdo.outlet.dashboard.data.DashboardData

/**
 * MVI Contract for the ASM Home Dashboard.
 * ASM sees more data points than ASE: total ASEs, active/suspended outlets, etc.
 */

sealed interface AsmHomeAction {
    data object LoadDashboard : AsmHomeAction
    data object TotalAsesClicked : AsmHomeAction
    data object TotalOutletsClicked : AsmHomeAction
    data object ActiveOutletsClicked : AsmHomeAction
    data object InProgressOutletsClicked : AsmHomeAction
    data object SuspendedOutletsClicked : AsmHomeAction
    data object AsmPendingOutletsClicked : AsmHomeAction
    data object PendingInvoicesClicked : AsmHomeAction
}

data class AsmHomeState(
    val isLoading: Boolean = false,
    val dashboard: DashboardData? = null,
    val slabs: List<SlabDto> = emptyList(),
    val isSlabsLoading: Boolean = false
)

sealed interface AsmHomeEvent {
    data object NavigateToMyAses : AsmHomeEvent
    data object NavigateToOutletsAll : AsmHomeEvent
    data object NavigateToOutletsActive : AsmHomeEvent
    data object NavigateToOutletsInProgress : AsmHomeEvent
    data object NavigateToOutletsSuspended : AsmHomeEvent
    data object NavigateToOutletsAsmPending : AsmHomeEvent
    data object NavigateToInvoicesPending : AsmHomeEvent
}
