package com.siteflow.retailsync.login.domain

import com.siteflow.retailsync.core.data.networking.result.onError
import com.siteflow.retailsync.core.data.networking.result.onSuccess
import com.siteflow.retailsync.core.domain.AuthRepository
import com.siteflow.retailsync.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.retailsync.login.data.LoginRepository
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
    private val authRepository: AuthRepository
) {

    private val _state = MutableStateFlow(OtpState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OtpEvent>()
    val events = _events.asSharedFlow()

    private var countdownJob: Job? = null

    fun setMobileNumber(number: String) {
        _state.update { it.copy(mobileNumber = number) }
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
            // Handle paste
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

                    // Fetch user profile
                    repository.fetchMe()
                        .onSuccess { userData ->
                            userData.name?.let { authRepository.saveUserName(it) }
                            // For DISTRIBUTOR role users, distributorId is null; use their own user id
                            val distId = userData.distributorId ?: userData.id
                            distId?.let { authRepository.saveDistributorId(it) }
                        }
                        .onError { meError ->
                            println("[AUTH] /users/me failed (non-blocking): ${meError.message}")
                        }

                    _state.update { it.copy(isLoading = false) }
                    GlobalToastHandler.showSuccess("Login successful")
                    _events.emit(OtpEvent.NavigateToDashboard)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    private fun resendOtp() {
        if (!_state.value.canResend) return
        scope.launch {
            _state.update { it.copy(error = null) }
            repository.requestOtp(phone = _state.value.mobileNumber, showLoader = false)
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
        scope.launch {
            countdownJob?.cancel()
            _events.emit(OtpEvent.NavigateBackToLogin)
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            _state.update { it.copy(resendCountdown = 30, canResend = false) }
            for (i in 30 downTo 0) {
                _state.update { it.copy(resendCountdown = i, canResend = i == 0) }
                if (i > 0) delay(1000)
            }
        }
    }

    private fun OtpState.recompute(): OtpState {
        return copy(isValid = otp.all { it.isNotEmpty() })
    }
}
