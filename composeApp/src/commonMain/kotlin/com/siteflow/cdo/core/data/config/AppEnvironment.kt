package com.siteflow.cdo.core.data.config

enum class AppEnvironment(
    val displayName: String,
    val apiHost: String,
    val badgeLabel: String
) {
    QA(
        displayName = "QA",
        apiHost = "https://qa.api.cdo.siteflow.tech",
        badgeLabel = "QA"
    ),
    UAT(
        displayName = "UAT",
        apiHost = "https://uat.api.cdo.siteflow.tech",
        badgeLabel = "UAT"
    ),
    PROD(
        displayName = "Production",
        apiHost = "https://api.cdo.siteflow.tech",
        badgeLabel = "PROD"
    );

    val baseUrl: String get() = "$apiHost/api/v1"

    companion object {
        fun fromKey(key: String): AppEnvironment =
            entries.firstOrNull { it.name == key } ?: PROD
    }
}
