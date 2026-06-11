package com.siteflow.cdo.login.domain

import com.siteflow.cdo.core.analytics.AnalyticsEvent
import com.siteflow.cdo.core.analytics.AnalyticsTracker
import com.siteflow.cdo.core.data.networking.result.onSuccess
import com.siteflow.cdo.core.data.networking.result.onError
import com.siteflow.cdo.core.presentation.BaseViewModel
import com.siteflow.cdo.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.cdo.login.data.LoginRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoginViewModel(
    private val scope: CoroutineScope,
    private val repository: LoginRepository,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<LoginState, LoginAction, LoginEvent>(LoginState()) {

    override fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.MobileNumberChanged -> updateMobileNumber(action.value)
            LoginAction.Submit -> submit()
        }
    }

    private fun updateMobileNumber(value: String) {
        updateState {
            it.copy(
                mobileNumber = value,
                error = null
            ).recompute()
        }
    }

    private fun submit() {
        analytics.track(AnalyticsEvent.GlobalEvent.LoginSubmitted(mobileNumberLength = state.value.mobileNumber.length))
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            repository.requestOtp(phone = state.value.mobileNumber, showLoader = false)
                .onSuccess { msg ->
                    updateState { it.copy(isLoading = false) }
                    GlobalToastHandler.showSuccess(msg)
                    emitEvent(LoginEvent.NavigateToOtp(state.value.mobileNumber))
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                    analytics.track(
                        AnalyticsEvent.GlobalEvent.LoginFailed(
                            errorMessage = error.message,
                            errorCode = error.code?.toString() ?: ""
                        )
                    )
                }
        }
    }

    private fun LoginState.recompute(): LoginState {
        val mobileOk = mobileNumber.length >= 10
        return copy(isValid = mobileOk)
    }
}
