package com.siteflow.cdo.ase.profile.data

import com.siteflow.cdo.core.data.networking.error.ApiError
import com.siteflow.cdo.core.data.networking.result.NetworkResult
import com.siteflow.cdo.core.domain.AuthRepository
import com.siteflow.cdo.core.domain.RoleManager
import com.siteflow.cdo.ase.profile.data.dto.TeamResponseDto
import com.siteflow.cdo.ase.profile.data.dto.UserMeResponseDto

class UserRepository(
    private val api: UserApi,
    private val authRepository: AuthRepository
) {
    suspend fun getMyAses(
        page: Int = 0,
        size: Int = 50
    ): NetworkResult<TeamResponseDto, ApiError> {
        return api.getMyAses(page, size)
    }

    suspend fun getMe(): NetworkResult<UserMeResponseDto, ApiError> {
        return api.getMe()
    }

    suspend fun deactivateOutlet(outletId: String): NetworkResult<Unit, ApiError> {
        return api.deactivateOutlet(outletId)
    }

    /** Clears all stored tokens, preferences, in-memory role, and cached data. */
    suspend fun clearSession() {
        authRepository.logout()     // Wipe persisted tokens & prefs
        RoleManager.clear()         // Wipe in-memory role
    }
}

