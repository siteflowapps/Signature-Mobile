package com.siteflow.signature.login.domain

import com.siteflow.signature.core.domain.UserRole

sealed interface OtpAction {
    data class DigitEntered(val index: Int, val value: String) : OtpAction
    data class BackspacePressed(val index: Int) : OtpAction
    data object Submit : OtpAction
    data object ResendOtp : OtpAction
    data object EditNumber : OtpAction
}

data class OtpState(
    val otp: List<String> = listOf("", "", "", "", "", ""),
    val mobileNumber: String = "",
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    val error: String? = null,
    val resendCountdown: Int = 60,
    val canResend: Boolean = false
) {
    val maskedNumber: String
        get() = "+91 $mobileNumber"
}

sealed interface OtpEvent {
    data class NavigateToDashboard(val role: UserRole) : OtpEvent
    data object NavigateBackToLogin : OtpEvent
    data object NavigateToWalkthrough : OtpEvent
}
