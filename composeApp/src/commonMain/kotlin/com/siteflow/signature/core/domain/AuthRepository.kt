package com.siteflow.signature.core.domain

import com.siteflow.signature.core.data.auth.TokenStorage

class AuthRepository(
    private val tokenStorage: TokenStorage
) {

    suspend fun saveAccessToken(token: String) {
        tokenStorage.saveAccessToken(token)
    }

    suspend fun saveRefreshToken(token: String) {
        tokenStorage.saveRefreshToken(token)
    }

    suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }

    suspend fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken()
    }

    suspend fun saveBusinessId(id: String) {
        tokenStorage.saveBusinessId(id)
    }

    suspend fun getBusinessId(): String? {
        return tokenStorage.getBusinessId()
    }

    suspend fun saveOutletId(id: String) {
        tokenStorage.saveOutletId(id)
    }

    suspend fun getOutletId(): String? {
        return tokenStorage.getOutletId()
    }

    suspend fun saveDistributorId(id: String) {
        tokenStorage.saveDistributorId(id)
    }

    suspend fun getDistributorId(): String? {
        return tokenStorage.getDistributorId()
    }

    suspend fun saveUserName(name: String) {
        tokenStorage.saveUserName(name)
    }

    suspend fun getUserName(): String? {
        return tokenStorage.getUserName()
    }


    suspend fun logout() {
        tokenStorage.clear()
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getAccessToken() != null
    }
}