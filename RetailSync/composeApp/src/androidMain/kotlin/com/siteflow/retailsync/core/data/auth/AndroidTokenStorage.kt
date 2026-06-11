package com.siteflow.retailsync.core.data.auth

import android.content.Context

class AndroidTokenStorage(
    context: Context
) : TokenStorage {

    private val prefs = context.getSharedPreferences(
        "retailsync_auth_prefs",
        Context.MODE_PRIVATE
    )

    override suspend fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    override suspend fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    override suspend fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    override suspend fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    override suspend fun saveDistributorId(id: String) {
        prefs.edit().putString(KEY_DISTRIBUTOR_ID, id).apply()
    }

    override suspend fun getDistributorId(): String? {
        return prefs.getString(KEY_DISTRIBUTOR_ID, null)
    }

    override suspend fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DISTRIBUTOR_ID = "distributor_id"
    }
}
