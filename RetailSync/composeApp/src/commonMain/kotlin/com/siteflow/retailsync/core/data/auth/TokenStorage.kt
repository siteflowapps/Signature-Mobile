package com.siteflow.retailsync.core.data.auth

interface TokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveUserName(name: String)
    suspend fun getUserName(): String?
    suspend fun saveDistributorId(id: String)
    suspend fun getDistributorId(): String?
    suspend fun clear()
}
