package com.siteflow.cdo.core.data.networking.client

import com.siteflow.cdo.core.data.config.EnvironmentManager

object NetworkConfig {
    const val TIMEOUT_MS = 120_000L   // OCR extraction can take up to ~90s

    /** Dynamic base URL — reads the active environment at call time. */
    val BASE_URL: String
        get() = EnvironmentManager.baseUrl

    fun v1(path: String): String = "${EnvironmentManager.apiHost}/api/v1/$path"
    fun v2(path: String): String = "${EnvironmentManager.apiHost}/api/v2/$path"
}

