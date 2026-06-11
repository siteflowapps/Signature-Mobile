package com.siteflow.cdo.ase.profile.domain

import com.siteflow.cdo.ase.profile.data.AseProfile
import com.siteflow.cdo.outlet.walkthrough.data.OutletKycData

/**
 * MVI Contract for the Profile feature.
 */

sealed interface ProfileAction {
    data object LoadProfile : ProfileAction
    data object LogoutTapped : ProfileAction
    data object Logout : ProfileAction
    data object DeleteAccountRequested : ProfileAction
    data object ConfirmDeleteAccount : ProfileAction
    data object DismissDeleteAccountDialog : ProfileAction
    data object MyTeamTapped : ProfileAction
}

data class ProfileState(
    val profile: AseProfile? = null,
    val isLoading: Boolean = false,
    val kycData: OutletKycData? = null,
    val isKycLoading: Boolean = false,
    val outletId: String? = null,
    val showDeleteAccountDialog: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null
)

sealed interface ProfileEvent {
    data object NavigateToLogin : ProfileEvent
    data object AccountDeleted : ProfileEvent
}
