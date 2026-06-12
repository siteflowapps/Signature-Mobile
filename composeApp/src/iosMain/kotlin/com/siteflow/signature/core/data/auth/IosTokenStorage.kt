package com.siteflow.signature.core.data.auth


import platform.Foundation.NSUserDefaults

class IosTokenStorage : TokenStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun saveAccessToken(token: String) {
        defaults.setObject(token, forKey = KEY_ACCESS_TOKEN)
    }

    override suspend fun getAccessToken(): String? =
        defaults.stringForKey(KEY_ACCESS_TOKEN)

    override suspend fun saveRefreshToken(token: String) {
        defaults.setObject(token, forKey = KEY_REFRESH_TOKEN)
    }

    override suspend fun getRefreshToken(): String? =
        defaults.stringForKey(KEY_REFRESH_TOKEN)

    override suspend fun saveBusinessId(id: String) {
        defaults.setObject(id, forKey = KEY_BUSINESS_ID)
    }

    override suspend fun getBusinessId(): String? =
        defaults.stringForKey(KEY_BUSINESS_ID)

    override suspend fun saveOutletId(id: String) {
        defaults.setObject(id, forKey = KEY_OUTLET_ID)
    }

    override suspend fun getOutletId(): String? =
        defaults.stringForKey(KEY_OUTLET_ID)

    override suspend fun saveDistributorId(id: String) {
        defaults.setObject(id, forKey = KEY_DISTRIBUTOR_ID)
    }

    override suspend fun getDistributorId(): String? =
        defaults.stringForKey(KEY_DISTRIBUTOR_ID)

    override suspend fun saveUserName(name: String) {
        defaults.setObject(name, forKey = KEY_USER_NAME)
    }

    override suspend fun getUserName(): String? =
        defaults.stringForKey(KEY_USER_NAME)

    override suspend fun saveWalkthroughCompleted(completed: Boolean) {
        defaults.setBool(completed, forKey = KEY_WALKTHROUGH_COMPLETED)
    }

    override suspend fun getWalkthroughCompleted(): Boolean {
        return defaults.boolForKey(KEY_WALKTHROUGH_COMPLETED)
    }

    override fun saveEnvironment(key: String) {
        defaults.setObject(key, forKey = KEY_ENVIRONMENT)
    }

    override fun getEnvironment(): String? =
        defaults.stringForKey(KEY_ENVIRONMENT)

    override suspend fun clear() {
        defaults.removeObjectForKey(KEY_ACCESS_TOKEN)
        defaults.removeObjectForKey(KEY_REFRESH_TOKEN)
        defaults.removeObjectForKey(KEY_BUSINESS_ID)
        defaults.removeObjectForKey(KEY_OUTLET_ID)
        defaults.removeObjectForKey(KEY_DISTRIBUTOR_ID)
        defaults.removeObjectForKey(KEY_USER_NAME)
        defaults.removeObjectForKey(KEY_WALKTHROUGH_COMPLETED)
        // KEY_ENVIRONMENT intentionally NOT cleared — env choice persists across logout
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
