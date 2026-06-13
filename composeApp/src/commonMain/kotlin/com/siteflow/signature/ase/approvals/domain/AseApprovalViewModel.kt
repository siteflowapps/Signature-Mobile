package com.siteflow.signature.ase.approvals.domain

import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * MVI contract + ViewModel for ASE Level-1 review of a single outlet.
 *
 * The ASE reaches this from the Outlets tab (the shared field dashboard, which
 * the backend auto-scopes to the ASE's team). We load the tapped outlet by id
 * and, when it's [OutletStatus.ASE_PENDING], allow approve/reject through the
 * unified `/decision` endpoint.
 */

sealed interface AseApprovalAction {
    data class Load(val outletId: String) : AseApprovalAction
    data class Approve(val outletId: String) : AseApprovalAction
    data class Reject(val outletId: String, val reason: String) : AseApprovalAction
}

data class AseApprovalState(
    val outlet: OutletItem? = null,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
) {
    val canDecide: Boolean get() = outlet?.status == OutletStatus.ASE_PENDING
}

sealed interface AseApprovalEvent {
    data class ShowMessage(val message: String) : AseApprovalEvent
    data object DecisionComplete : AseApprovalEvent
}

class AseApprovalViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository
) : BaseViewModel<AseApprovalState, AseApprovalAction, AseApprovalEvent>(AseApprovalState()) {

    override fun onAction(action: AseApprovalAction) {
        when (action) {
            is AseApprovalAction.Load -> loadOutlet(action.outletId)
            is AseApprovalAction.Approve -> decide(action.outletId, "APPROVE", null)
            is AseApprovalAction.Reject -> decide(action.outletId, "REJECT", action.reason)
        }
    }

    private fun loadOutlet(outletId: String) {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            outletRepository.getOutletById(outletId)
                .onSuccess { data ->
                    updateState { it.copy(outlet = OutletItem.fromDto(data), isLoading = false) }
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun decide(outletId: String, action: String, reason: String?) {
        scope.launch {
            updateState { it.copy(isProcessing = true) }
            outletRepository.decide(outletId, action, reason)
                .onSuccess {
                    updateState { it.copy(isProcessing = false) }
                    val msg = if (action == "APPROVE") "Outlet approved" else "Outlet rejected"
                    GlobalToastHandler.showSuccess(msg)
                    _events.tryEmit(AseApprovalEvent.DecisionComplete)
                }
                .onError { error ->
                    updateState { it.copy(isProcessing = false, error = error.message) }
                    GlobalToastHandler.showError(error.message ?: "Action failed")
                }
        }
    }
}
