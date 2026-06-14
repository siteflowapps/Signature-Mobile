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
 * Drives one asset-request section (COOLER or MARKETING) on the outlet detail.
 * Loads the outlet's current request of that kind and lets the current role act
 * at the stage they own: ASE on REQUESTED (L1), ASM on ASE_APPROVED (L2).
 * Raise / compliance-upload are triggered by the parent screen (forms/picker).
 */

sealed interface OutletAssetAction {
    data class Load(val outletId: String, val kind: String) : OutletAssetAction
    data class Approve(val requestId: String) : OutletAssetAction
    data class Reject(val requestId: String, val reason: String) : OutletAssetAction
}

data class OutletAssetState(
    val request: AssetRequestItemDto? = null,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false
) {
    val canDecide: Boolean
        get() = when (RoleManager.currentRole.value) {
            UserRole.ASE -> request?.status == "REQUESTED"
            UserRole.ASM -> request?.status == "ASE_APPROVED"
            else -> false
        }

    /** CSO can upload compliance once installed (or to fix overdue/non-compliant). */
    val needsCompliance: Boolean
        get() = request?.status in setOf("EXECUTED", "COMPLIANCE_OVERDUE", "NON_COMPLIANT")
}

class OutletAssetViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository
) : BaseViewModel<OutletAssetState, OutletAssetAction, Unit>(OutletAssetState()) {

    private var lastOutletId: String? = null
    private var lastKind: String? = null

    override fun onAction(action: OutletAssetAction) {
        when (action) {
            is OutletAssetAction.Load -> load(action.outletId, action.kind)
            is OutletAssetAction.Approve -> decide(action.requestId, "APPROVE", null)
            is OutletAssetAction.Reject -> decide(action.requestId, "REJECT", action.reason)
        }
    }

    private fun load(outletId: String, kind: String) {
        if (outletId.isBlank()) return
        lastOutletId = outletId
        lastKind = kind
        scope.launch {
            updateState { it.copy(isLoading = true) }
            outletRepository.getOutletAssetRequest(outletId, kind)
                .onSuccess { req -> updateState { it.copy(request = req, isLoading = false) } }
                .onError { updateState { it.copy(isLoading = false) } }
        }
    }

    /** Re-fetch the current request (e.g. after the parent raises one or uploads compliance). */
    fun refresh() {
        val id = lastOutletId ?: return
        val kind = lastKind ?: return
        load(id, kind)
    }

    private fun decide(requestId: String, action: String, reason: String?) {
        scope.launch {
            updateState { it.copy(isProcessing = true) }
            outletRepository.decideAssetRequest(requestId, action, reason)
                .onSuccess {
                    updateState { it.copy(isProcessing = false) }
                    GlobalToastHandler.showSuccess(
                        if (action == "APPROVE") "Request approved" else "Request rejected"
                    )
                    refresh()
                }
                .onError { error ->
                    updateState { it.copy(isProcessing = false) }
                    GlobalToastHandler.showError(error.message ?: "Action failed")
                }
        }
    }
}
