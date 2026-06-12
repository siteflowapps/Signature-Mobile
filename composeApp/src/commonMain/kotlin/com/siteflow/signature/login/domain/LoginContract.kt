package com.siteflow.signature.login.domain


sealed interface LoginAction {
    data class MobileNumberChanged(val value: String) : LoginAction
    data object Submit : LoginAction
}

data class LoginState(
    val mobileNumber: String = "",
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    val error: String? = null
)

sealed interface LoginEvent {
    data class NavigateToOtp(val mobileNumber: String) : LoginEvent
}
