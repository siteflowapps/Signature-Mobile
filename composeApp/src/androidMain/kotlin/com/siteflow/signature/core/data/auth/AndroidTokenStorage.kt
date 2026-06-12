package com.siteflow.signature.core.data.auth

import android.content.Context

class AndroidTokenStorage(
    context: Context
) : TokenStorage {

    private val prefs = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )

    override suspend fun saveAccessToken(token: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    override suspend fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun saveRefreshToken(token: String) {
        prefs.edit()
            .putString(KEY_REFRESH_TOKEN, token)
            .apply()
    }

    override suspend fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun saveBusinessId(id: String) {
        prefs.edit()
            .putString(KEY_BUSINESS_ID, id)
            .apply()
    }

    override suspend fun getBusinessId(): String? {
        return prefs.getString(KEY_BUSINESS_ID, null)
    }

    override suspend fun saveOutletId(id: String) {
        prefs.edit()
            .putString(KEY_OUTLET_ID, id)
            .apply()
    }

    override suspend fun getOutletId(): String? {
        return prefs.getString(KEY_OUTLET_ID, null)
    }

    override suspend fun saveDistributorId(id: String) {
        prefs.edit()
            .putString(KEY_DISTRIBUTOR_ID, id)
            .apply()
    }

    override suspend fun getDistributorId(): String? {
        return prefs.getString(KEY_DISTRIBUTOR_ID, null)
    }

    override suspend fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    override suspend fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    override suspend fun saveWalkthroughCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_WALKTHROUGH_COMPLETED, completed).apply()
    }

    override suspend fun getWalkthroughCompleted(): Boolean {
        return prefs.getBoolean(KEY_WALKTHROUGH_COMPLETED, false)
    }

    override fun saveEnvironment(key: String) {
        prefs.edit().putString(KEY_ENVIRONMENT, key).apply()
    }

    override fun getEnvironment(): String? =
        prefs.getString(KEY_ENVIRONMENT, null)

    override suspend fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_BUSINESS_ID)
            .remove(KEY_OUTLET_ID)
            .remove(KEY_DISTRIBUTOR_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_WALKTHROUGH_COMPLETED)
            // KEY_ENVIRONMENT intentionally NOT cleared — env choice persists across logout
            .apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_BUSINESS_ID = "business_id"
        private const val KEY_OUTLET_ID = "outlet_id"
        private const val KEY_DISTRIBUTOR_ID = "distributor_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_WALKTHROUGH_COMPLETED = "walkthrough_completed"
        private const val KEY_ENVIRONMENT = "app_environment"
    }
}
