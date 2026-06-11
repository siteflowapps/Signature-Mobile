package com.siteflow.cdo.core.domain

import com.siteflow.cdo.core.data.config.ConfigApi
import com.siteflow.cdo.core.data.networking.result.onError
import com.siteflow.cdo.core.data.networking.result.onSuccess
import com.siteflow.cdo.core.data.networking.util.JwtUtils
import com.siteflow.cdo.core.navigation.AppDestination
import com.siteflow.cdo.core.presentation.BaseViewModel
import com.siteflow.cdo.outlet.walkthrough.domain.WalkthroughRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class AppStartState(val startDestination: String? = null)
sealed interface AppStartAction {
    data object ResolveStartDestination : AppStartAction
}

class AppStartViewModel(
    private val scope: CoroutineScope,
    private val authRepository: AuthRepository,
    private val walkthroughRepository: WalkthroughRepository,
    private val configApi: ConfigApi
) : BaseViewModel<AppStartState, AppStartAction, Unit>(AppStartState()) {

    override fun onAction(action: AppStartAction) {
        when (action) {
            AppStartAction.ResolveStartDestination -> resolveStartDestination()
        }
    }

    private fun resolveStartDestination() {
        scope.launch {
            val token = authRepository.getAccessToken()
            if (token != null) {
                val role = JwtUtils.extractRole(token)
                if (role != null) {
                    RoleManager.setRole(role)
                    val dest = when (role) {
                        UserRole.ASE -> AppDestination.AseHome.route
                        UserRole.ASM -> AppDestination.AsmHome.route
                        UserRole.OUTLET -> {
                            // Sync walkthrough flags from config API
                            configApi.getConfig()
                                .onSuccess { configResponse ->
                                    val userFlags = configResponse.data?.userFlags ?: emptyMap()
                                    walkthroughRepository.syncFromConfig(userFlags)
                                }
                                .onError { configError ->
                                    println("[AppStart] GET /config failed (using local cache): ${configError.message}")
                                }
                            val walkthroughDone = walkthroughRepository.isCompleted()
                            if (walkthroughDone) AppDestination.OutletDashboard.route
                            else AppDestination.WalkthroughWelcome.route
                        }
                    }
                    updateState { it.copy(startDestination = dest) }
                } else {
                    // Token exists but invalid role — force re-login
                    authRepository.logout()
                    updateState { it.copy(startDestination = AppDestination.Login.route) }
                }
            } else {
                updateState { it.copy(startDestination = AppDestination.Login.route) }
            }
        }
    }
}
