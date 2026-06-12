package com.siteflow.signature.login.domain

import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsParams
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.analytics.DeviceInfo
import com.siteflow.signature.core.data.config.ConfigApi
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.domain.AuthRepository
import com.siteflow.signature.core.domain.RoleManager
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.signature.login.data.LoginRepository
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpViewModel(
    private val scope: CoroutineScope,
    private val repository: LoginRepository,
    private val authRepository: AuthRepository,
    private val walkthroughRepository: WalkthroughRepository,
    private val configApi: ConfigApi,
    private val analytics: AnalyticsTracker,
    private val deviceInfo: DeviceInfo,
) {

    private val _state = MutableStateFlow(OtpState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OtpEvent>()
    val events = _events.asSharedFlow()

    private var countdownJob: Job? = null
    private var resendAttemptCount = 0

    fun setMobileNumber(number: String) {
        _state.update { it.copy(mobileNumber = number) }
        val masked = if (number.length >= 4) number.take(2) + "******" + number.takeLast(2) else "****"
        analytics.track(AnalyticsEvent.GlobalEvent.OtpScreenViewed(mobileNumberMasked = masked))
        startCountdown()
    }

    fun onAction(action: OtpAction) {
        when (action) {
            is OtpAction.DigitEntered -> enterDigit(action.index, action.value)
            is OtpAction.BackspacePressed -> handleBackspace(action.index)
            OtpAction.Submit -> submit()
            OtpAction.ResendOtp -> resendOtp()
            OtpAction.EditNumber -> editNumber()
        }
    }

    private fun enterDigit(index: Int, value: String) {
        if (value.length > 1) {
            // Handle paste: distribute digits across boxes
            val digits = value.filter { it.isDigit() }.take(6)
            val newOtp = _state.value.otp.toMutableList()
            digits.forEachIndexed { i, char ->
                val targetIndex = index + i
                if (targetIndex < 6) {
                    newOtp[targetIndex] = char.toString()
                }
            }
            _state.update {
                it.copy(otp = newOtp, error = null).recompute()
            }
            return
        }

        if (value.isEmpty() || !value.first().isDigit()) return
        val newOtp = _state.value.otp.toMutableList()
        newOtp[index] = value.take(1)
        _state.update {
            it.copy(otp = newOtp, error = null).recompute()
        }
    }

    private fun handleBackspace(index: Int) {
        val newOtp = _state.value.otp.toMutableList()
        newOtp[index] = ""
        _state.update {
            it.copy(otp = newOtp, error = null).recompute()
        }
    }

    private fun submit() {
        val currentOtp = _state.value.otp.joinToString("")
        if (currentOtp.length < 6) return

        analytics.track(AnalyticsEvent.GlobalEvent.OtpEntered(timeToEnterMs = 0L))

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.verifyOtp(
                phone = _state.value.mobileNumber,
                otp = currentOtp,
                showLoader = false
            )
                .onSuccess { tokenData ->
                    // Save tokens
                    authRepository.saveAccessToken(tokenData.accessToken)
                    authRepository.saveRefreshToken(tokenData.refreshToken)

                    // Save businessId from JWT
                    val businessId = com.siteflow.signature.core.data.networking.util.JwtUtils.extractBusinessId(tokenData.accessToken)
                    if (!businessId.isNullOrBlank()) {
                        authRepository.saveBusinessId(businessId)
                    }

                    // Fetch user profile to get outletId and distributorId
                    repository.fetchMe()
                        .onSuccess { userData ->
                            println("[AUTH] /users/me success: outletId=${userData.outletId}, distributorId=${userData.distributorId}, name=${userData.name}")
                            userData.outletId?.let { authRepository.saveOutletId(it) }
                            userData.distributorId?.let { authRepository.saveDistributorId(it) }
                            userData.name?.let { authRepository.saveUserName(it) }
                        }
                        .onError { meError ->
                            println("[AUTH] /users/me failed (non-blocking): ${meError.message}")
                        }

                    // Extract and set role
                    val role = try {
                        UserRole.valueOf(tokenData.role)
                    } catch (e: Exception) {
                        // Fallback: try parsing from JWT
                        com.siteflow.signature.core.data.networking.util.JwtUtils.extractRole(tokenData.accessToken)
                    }

                    if (role != null) {
                        RoleManager.setRole(role)

                        // Set user identity and properties for analytics
                        val resolvedBusinessId = businessId ?: ""
                        analytics.identify(resolvedBusinessId)
                        analytics.setUserProperty(AnalyticsParams.USER_ROLE, role.name)
                        analytics.setUserProperty(AnalyticsParams.BUSINESS_ID, resolvedBusinessId)
                        analytics.setUserProperty(AnalyticsParams.APP_VERSION, deviceInfo.appVersion)
                        analytics.setUserProperty(AnalyticsParams.PLATFORM, deviceInfo.platform)
                        analytics.setUserProperty(AnalyticsParams.OS_VERSION, deviceInfo.osVersion)
                        analytics.setUserProperty(AnalyticsParams.DEVICE_MODEL, deviceInfo.deviceModel)
                        analytics.track(AnalyticsEvent.GlobalEvent.OtpVerified(userRole = role.name, timeToVerifyMs = 0L, isFirstLogin = false))

                        // Fetch config and sync walkthrough flags
                        configApi.getConfig()
                            .onSuccess { configResponse ->
                                val userFlags = configResponse.data?.userFlags ?: emptyMap()
                                walkthroughRepository.syncFromConfig(userFlags)
                            }
                            .onError { configError ->
                                println("[AUTH] GET /config failed (non-blocking): ${configError.message}")
                            }

                        _state.update { it.copy(isLoading = false) }
                        GlobalToastHandler.showSuccess("Login successful")
                        if (role == UserRole.OUTLET && !walkthroughRepository.isCompleted()) {
                            _events.emit(OtpEvent.NavigateToWalkthrough)
                        } else {
                            _events.emit(OtpEvent.NavigateToDashboard(role))
                        }
                    } else {
                        authRepository.logout()
                        val friendlyMsg = "This app is not available for your role. Please contact your administrator."
                        _state.update {
                            it.copy(isLoading = false, error = friendlyMsg)
                        }
                        GlobalToastHandler.showError(friendlyMsg)
                    }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                    analytics.track(
                        AnalyticsEvent.GlobalEvent.OtpVerificationFailed(
                            errorMessage = error.message,
                            attemptNumber = 1
                        )
                    )
                }
        }
    }

    private fun resendOtp() {
        if (!_state.value.canResend) return
        resendAttemptCount++
        analytics.track(AnalyticsEvent.GlobalEvent.OtpResendTapped(resendAttemptNumber = resendAttemptCount, countdownRemaining = _state.value.resendCountdown))
        scope.launch {
            _state.update { it.copy(error = null) }
            repository.retryOtp(phone = _state.value.mobileNumber)
                .onSuccess { msg ->
                    GlobalToastHandler.showSuccess(msg)
                    startCountdown()
                }
                .onError { error ->
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    private fun editNumber() {
        analytics.track(AnalyticsEvent.GlobalEvent.OtpEditNumberTapped)
        scope.launch {
            countdownJob?.cancel()
            _events.emit(OtpEvent.NavigateBackToLogin)
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            _state.update { it.copy(resendCountdown = 60, canResend = false) }
            for (i in 60 downTo 0) {
                _state.update { it.copy(resendCountdown = i, canResend = i == 0) }
                if (i > 0) delay(1000)
            }
        }
    }

    private fun OtpState.recompute(): OtpState {
        return copy(isValid = otp.all { it.isNotEmpty() })
    }
}
