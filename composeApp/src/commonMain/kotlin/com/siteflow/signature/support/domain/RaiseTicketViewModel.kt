package com.siteflow.signature.support.domain

import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.domain.AuthRepository
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.signature.getPlatform
import com.siteflow.signature.support.data.SupportRepository
import com.siteflow.signature.support.data.SupportTicketData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RaiseTicketViewModel(
    private val scope: CoroutineScope,
    private val repository: SupportRepository,
    private val authRepository: AuthRepository
) : BaseViewModel<RaiseTicketState, RaiseTicketAction, RaiseTicketEvent>(RaiseTicketState()) {

    init {
        scope.launch {
            val userName = authRepository.getUserName() ?: ""
            val platform = getPlatform()
            updateState {
                it.copy(
                    userName = userName,
                    appVersion = platform.appVersion,
                    platform = platform.clientType,
                    osVersion = platform.osVersion,
                    deviceModel = platform.deviceModel
                )
            }
        }
    }

    override fun onAction(action: RaiseTicketAction) {
        when (action) {
            is RaiseTicketAction.IssueTypeSelected ->
                updateState { it.copy(issueType = action.type, error = null) }

            is RaiseTicketAction.DescriptionChanged -> {
                if (action.text.length <= 500) {
                    updateState { it.copy(description = action.text, error = null) }
                }
            }

            is RaiseTicketAction.ImageAdded ->
                updateState { s ->
                    if (s.attachments.size < 3) s.copy(attachments = s.attachments + action.localPath)
                    else s
                }

            is RaiseTicketAction.ImageRemoved ->
                updateState { it.copy(attachments = it.attachments - action.localPath) }

            RaiseTicketAction.Submit -> submit()

            RaiseTicketAction.DismissError ->
                updateState { it.copy(error = null) }
        }
    }

    private fun submit() {
        val s = state.value
        if (!s.canSubmit) return

        scope.launch {
            updateState { it.copy(isSubmitting = true, error = null) }

            repository.createTicket(
                data = SupportTicketData(
                    category = s.issueType!!.label,
                    description = s.description,
                    appVersion = s.appVersion,
                    platform = s.platform,
                    osVersion = s.osVersion,
                    deviceModel = s.deviceModel
                ),
                screenshotPaths = s.attachments
            )
                .onSuccess { response ->
                    updateState {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            ticketNumber = response.data?.ticketNumber
                        )
                    }
                }
                .onError { error ->
                    updateState { it.copy(isSubmitting = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }
}
