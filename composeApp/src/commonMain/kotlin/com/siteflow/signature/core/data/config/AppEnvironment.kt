package com.siteflow.signature.core.data.config

enum class AppEnvironment(
    val displayName: String,
    val apiHost: String,
    val badgeLabel: String
) {
    QA(
        displayName = "QA",
        apiHost = "https://qa.api.signature.siteflow.tech",
        badgeLabel = "QA"
    ),
    UAT(
        displayName = "UAT",
        apiHost = "https://uat.api.signature.siteflow.tech",
        badgeLabel = "UAT"
    ),
    PROD(
        displayName = "Production",
        apiHost = "https://api.signature.siteflow.tech",
        badgeLabel = "PROD"
    );

    val baseUrl: String get() = "$apiHost/api/v1"

    companion object {
        fun fromKey(key: String): AppEnvironment =
            entries.firstOrNull { it.name == key } ?: PROD

        /**
         * Default API environment derived from the build flavor
         * ([com.siteflow.signature.buildEnvironment]) when nothing is persisted.
         * - Android `BuildConfig.BUILD_TYPE`: "debug" / "uat" / "release"
         * - iOS `FIREBASE_ENV`: "QA" / "UAT" / "PROD"
         * Unknown values fall back to QA so a fresh debug install never points
         * at PROD (which is what caused login 503s on iOS).
         */
        fun fromBuild(build: String): AppEnvironment = when (build.uppercase()) {
            "QA", "DEBUG" -> QA
            "UAT" -> UAT
            "PROD", "RELEASE" -> PROD
            else -> QA
        }
    }
}
