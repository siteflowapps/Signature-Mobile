package com.siteflow.cdo

import android.content.Context
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val appVersion: String get() = _appVersion
    override val clientType: String = "android"
    override val osVersion: String = "Android ${Build.VERSION.RELEASE}"
    override val deviceModel: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    companion object {
        private var _appVersion: String = "1.0.0"

        fun init(context: Context) {
            _appVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
            } catch (_: Exception) {
                "1.0.0"
            }
        }
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val buildEnvironment: String = BuildConfig.BUILD_TYPE