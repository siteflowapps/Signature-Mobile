package com.siteflow.signature.asset.approvals.domain

import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.domain.RoleManager
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.cso.onboarding.data.dto.AssetRequestItemDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * MVI contract + ViewModel for the ASE/ASM cooler-request approval queue.
 *
 * The list is team-scoped by the backend. We filter by the status the current
 * role can act on: ASE → REQUESTED (L1), ASM → ASE_APPROVED (L2). Approve/reject
 * go through POST /asset-requests/{id}/decision.
 */

sealed interface AssetApprovalAction {
    /** kind = "COOLER" | "MARKETING" — sets the queue this finder shows. */
    data class Load(val kind: String) : AssetApprovalAction
    /** Switch the status pill; null = All stages. */
    data class SelectStatus(val status: String?) : AssetApprovalAction
    data object Refresh : AssetApprovalAction
    data class Approve(val requestId: String) : AssetApprovalAction
    data class Reject(val requestId: String, val reason: String) : AssetApprovalAction
}

data class AssetApprovalState(
    val requests: List<AssetRequestItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    /** Currently selected status pill; null = All stages. */
    val selectedStatus: String? = null
)

sealed interface AssetApprovalEvent {
    data class ShowMessage(val message: String) : AssetApprovalEvent
}

class AssetApprovalViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository
) : BaseViewModel<AssetApprovalState, AssetApprovalAction, AssetApprovalEvent>(AssetApprovalState()) {

    /** The asset kind this finder is showing (set by the first Load). */
    private var kind: String = "COOLER"

    /** Default pill — the stage this role acts on. */
    private fun defaultStatusForRole(): String? = when (RoleManager.currentRole.value) {
        UserRole.ASE -> "REQUESTED"
        UserRole.ASM -> "ASE_APPROVED"
        else -> null
    }

    override fun onAction(action: AssetApprovalAction) {
        when (action) {
            is AssetApprovalAction.Load -> {
                kind = action.kind
                updateState { it.copy(selectedStatus = defaultStatusForRole()) }
                load(refresh = false)
            }
            is AssetApprovalAction.SelectStatus -> {
                updateState { it.copy(selectedStatus = action.status) }
                load(refresh = false)
            }
            AssetApprovalAction.Refresh -> load(refresh = true)
            is AssetApprovalAction.Approve -> decide(action.requestId, "APPROVE", null)
            is AssetApprovalAction.Reject -> decide(action.requestId, "REJECT", action.reason)
        }
    }

    private fun load(refresh: Boolean) {
        val status = state.value.selectedStatus  // null = All stages
        scope.launch {
            updateState { it.copy(isLoading = !refresh, isRefreshing = refresh, error = null) }
            outletRepository.getAssetRequestsForApproval(status, kind)
                .onSuccess { list ->
                    updateState { it.copy(requests = list, isLoading = false, isRefreshing = false) }
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, isRefreshing = false, error = error.message) }
                }
        }
    }

    private fun decide(requestId: String, action: String, reason: String?) {
        scope.launch {
            updateState { it.copy(isProcessing = true) }
            outletRepository.decideAssetRequest(requestId, action, reason)
                .onSuccess {
                    updateState { it.copy(isProcessing = false) }
                    val msg = if (action == "APPROVE") "Request approved" else "Request rejected"
                    GlobalToastHandler.showSuccess(msg)
                    load(refresh = true)
                }
                .onError { error ->
                    updateState { it.copy(isProcessing = false, error = error.message) }
                    GlobalToastHandler.showError(error.message ?: "Action failed")
                }
        }
    }
}
