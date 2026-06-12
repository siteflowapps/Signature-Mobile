package com.siteflow.signature.cso.profile.domain

import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.cso.profile.data.*
import com.siteflow.signature.cso.profile.data.dto.UserMeDetailsDto
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.domain.AuthRepository
import com.siteflow.signature.core.domain.RoleManager
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.core.presentation.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile screen.
 * Loads user profile + KYC bank details for OUTLET role.
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val outletRepository: OutletRepository,
    private val authRepository: AuthRepository,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<ProfileState, ProfileAction, ProfileEvent>(
    initialState = ProfileState()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.LoadProfile -> loadProfile()
            ProfileAction.LogoutTapped -> analytics.track(AnalyticsEvent.GlobalEvent.LogoutTapped(userRole = RoleManager.currentRole.value?.name ?: ""))
            ProfileAction.Logout -> logout()
            ProfileAction.DeleteAccountRequested -> updateState { it.copy(showDeleteAccountDialog = true, deleteAccountError = null) }
            ProfileAction.DismissDeleteAccountDialog -> updateState { it.copy(showDeleteAccountDialog = false) }
            ProfileAction.ConfirmDeleteAccount -> deleteAccount()
            ProfileAction.MyTeamTapped -> analytics.track(AnalyticsEvent.ProfileEvent.ProfileMyTeamTapped)
        }
    }

    private fun loadProfile() {
        updateState { it.copy(isLoading = true) }

        scope.launch {
            userRepository.getMe()
                .onSuccess { response ->
                    val details = response.data
                    if (details != null) {
                        val currentRole = RoleManager.currentRole.value
                        val mappedProfile = details.toAseProfile(currentRole)
                        updateState { it.copy(profile = mappedProfile, isLoading = false) }

                        val hasKyc = currentRole == UserRole.OUTLET
                        analytics.track(AnalyticsEvent.ProfileEvent.ProfileViewed(
                            userRole = currentRole?.name ?: "",
                            hasKycData = hasKyc
                        ))

                        // Fetch KYC for outlet role
                        if (currentRole == UserRole.OUTLET) {
                            val outletId = authRepository.getOutletId()
                            updateState { it.copy(outletId = outletId) }
                            loadKyc()
                        }
                    } else {
                        updateState { it.copy(isLoading = false) }
                    }
                }
                .onError {
                    println("[ProfileVM] Error loading profile")
                    updateState { it.copy(isLoading = false) }
                }
        }
    }

    private fun loadKyc() {
        scope.launch {
            val outletId = authRepository.getOutletId() ?: return@launch
            updateState { it.copy(isKycLoading = true) }

            outletRepository.getOutletKyc(outletId)
                .onSuccess { kycData ->
                    updateState { it.copy(kycData = kycData, isKycLoading = false) }
                }
                .onError {
                    println("[ProfileVM] Error loading KYC")
                    updateState { it.copy(isKycLoading = false) }
                }
        }
    }

    private fun UserMeDetailsDto.toAseProfile(currentRole: UserRole?): AseProfile {
        return AseProfile(
            name = this.name,
            role = this.role,
            department = when (currentRole) {
                UserRole.ASM -> "Sales Management"
                UserRole.OUTLET -> "PFP Partner"
                else -> "CSD Department"
            },
            employeeId = this.id.take(8).uppercase(),
            location = "",
            initials = this.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").uppercase(),
            kpis = when (currentRole) {
                UserRole.ASM -> mockAsmProfile.kpis
                UserRole.OUTLET -> emptyList()
                else -> mockAseProfile.kpis
            },
            outletProfile = if (currentRole == UserRole.OUTLET) {
                mockOutletProfile.outletProfile?.copy(
                    ownerName = this.name
                )
            } else null
        )
    }

    private fun logout() {
        scope.launch {
            analytics.track(AnalyticsEvent.GlobalEvent.LogoutConfirmed())
            analytics.reset()
            userRepository.clearSession()
            emitEvent(ProfileEvent.NavigateToLogin)
        }
    }

    private fun deleteAccount() {
        scope.launch {
            val outletId = authRepository.getOutletId()
            if (outletId == null) {
                updateState {
                    it.copy(
                        isDeletingAccount = false,
                        deleteAccountError = "Unable to identify outlet. Please try again."
                    )
                }
                return@launch
            }

            updateState { it.copy(isDeletingAccount = true, showDeleteAccountDialog = false) }
            userRepository.deactivateOutlet(outletId)
                .onSuccess {
                    userRepository.clearSession()
                    emitEvent(ProfileEvent.AccountDeleted)
                }
                .onError {
                    println("[ProfileVM] Error deactivating outlet: $it")
                    updateState {
                        it.copy(
                            isDeletingAccount = false,
                            deleteAccountError = "Failed to delete account. Please try again."
                        )
                    }
                }
        }
    }
}
