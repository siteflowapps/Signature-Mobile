package com.siteflow.cdo.core.data.auth

interface TokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveBusinessId(id: String)
    suspend fun getBusinessId(): String?
    suspend fun saveOutletId(id: String)
    suspend fun getOutletId(): String?
    suspend fun saveDistributorId(id: String)
    suspend fun getDistributorId(): String?
    suspend fun saveUserName(name: String)
    suspend fun getUserName(): String?
    suspend fun saveWalkthroughCompleted(completed: Boolean)
    suspend fun getWalkthroughCompleted(): Boolean
    /** Persists the environment key (non-suspend — sync prefs write). */
    fun saveEnvironment(key: String)
    fun getEnvironment(): String?
    suspend fun clear()
}