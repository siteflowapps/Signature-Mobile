package com.siteflow.retailsync.core.data.auth

import platform.Foundation.NSUserDefaults

class IosTokenStorage : TokenStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun saveAccessToken(token: String) {
        defaults.setObject(token, KEY_ACCESS_TOKEN)
    }

    override suspend fun getAccessToken(): String? {
        return defaults.stringForKey(KEY_ACCESS_TOKEN)
    }

    override suspend fun saveRefreshToken(token: String) {
        defaults.setObject(token, KEY_REFRESH_TOKEN)
    }

    override suspend fun getRefreshToken(): String? {
        return defaults.stringForKey(KEY_REFRESH_TOKEN)
    }

    override suspend fun saveUserName(name: String) {
        defaults.setObject(name, KEY_USER_NAME)
    }

    override suspend fun getUserName(): String? {
        return defaults.stringForKey(KEY_USER_NAME)
    }

    override suspend fun saveDistributorId(id: String) {
        defaults.setObject(id, KEY_DISTRIBUTOR_ID)
    }

    override suspend fun getDistributorId(): String? {
        return defaults.stringForKey(KEY_DISTRIBUTOR_ID)
    }

    override suspend fun clear() {
        defaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        defaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        defaults.removeObjectForKey(KEY_USER_NAME)
        defaults.removeObjectForKey(KEY_DISTRIBUTOR_ID)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "retailsync_access_token"
        private const val KEY_REFRESH_TOKEN = "retailsync_refresh_token"
        private const val KEY_USER_NAME = "retailsync_user_name"
        private const val KEY_DISTRIBUTOR_ID = "retailsync_distributor_id"
    }
}
