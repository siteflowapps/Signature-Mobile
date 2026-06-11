package com.siteflow.retailsync.login.domain

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
    val resendCountdown: Int = 30,
    val canResend: Boolean = false
) {
    val maskedNumber: String
        get() {
            if (mobileNumber.length < 10) return "+91 $mobileNumber"
            return "+91 ${mobileNumber.take(2)}XXXXX${mobileNumber.takeLast(2)}"
        }
}

sealed interface OtpEvent {
    data object NavigateToDashboard : OtpEvent
    data object NavigateBackToLogin : OtpEvent
}
