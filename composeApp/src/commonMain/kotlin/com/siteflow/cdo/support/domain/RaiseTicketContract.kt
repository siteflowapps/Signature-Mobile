package com.siteflow.cdo.support.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

enum class IssueType(val label: String, val icon: ImageVector) {
    APP_ISSUE("App Issue", Icons.Default.PhoneAndroid),
    INVOICE_ISSUE("Invoice Issue", Icons.Default.Description),
    PAYOUT_ISSUE("Payout Issue", Icons.Default.AccountBalance),
    ONBOARDING_ISSUE("Onboarding Issue", Icons.Default.PersonAdd),
    LOGIN_OTP_ISSUE("Login / OTP", Icons.Default.Lock),
    PERFORMANCE_ISSUE("Performance", Icons.Default.Warning),
    DATA_INCORRECT("Data Incorrect", Icons.Default.Error),
    OTHER("Other", Icons.AutoMirrored.Filled.HelpOutline)
}

data class RaiseTicketState(
    val issueType: IssueType? = null,
    val description: String = "",
    val attachments: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val ticketNumber: String? = null,
    val error: String? = null,
    val userName: String = "",
    val appVersion: String = "",
    val platform: String = "",
    val osVersion: String = "",
    val deviceModel: String = ""
) {
    val canSubmit: Boolean get() = issueType != null && description.length >= 20
}

sealed interface RaiseTicketAction {
    data class IssueTypeSelected(val type: IssueType) : RaiseTicketAction
    data class DescriptionChanged(val text: String) : RaiseTicketAction
    data class ImageAdded(val localPath: String) : RaiseTicketAction
    data class ImageRemoved(val localPath: String) : RaiseTicketAction
    data object Submit : RaiseTicketAction
    data object DismissError : RaiseTicketAction
}

sealed interface RaiseTicketEvent {
    data object NavigateBack : RaiseTicketEvent
}
