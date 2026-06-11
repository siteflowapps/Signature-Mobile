package com.siteflow.cdo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.siteflow.cdo.core.analytics.AnalyticsEvent
import com.siteflow.cdo.core.analytics.AnalyticsTracker
import com.siteflow.cdo.core.analytics.DeviceInfo
import com.siteflow.cdo.core.domain.AuthRepository
import com.siteflow.cdo.core.domain.ImagePicker
import com.siteflow.cdo.core.domain.LocationPermissionHelper
import com.siteflow.cdo.core.domain.PdfPicker
import com.siteflow.cdo.core.domain.RoleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class MainActivity : ComponentActivity() {

    private val analytics: AnalyticsTracker by inject()
    private val deviceInfo: DeviceInfo by inject()
    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        val imagePicker = ImagePicker().apply {
            bind(this@MainActivity)
        }

        val pdfPicker = PdfPicker().apply {
            bind(this@MainActivity)
        }

        val locationPermissionHelper = LocationPermissionHelper(this)

        loadKoinModules(
            module {
                single { imagePicker }
                single { pdfPicker }
                single { locationPermissionHelper }
            }
        )

        installCrashHandler()

        val isFreshInstall = savedInstanceState == null &&
            getSharedPreferences("analytics_prefs", MODE_PRIVATE)
                .getBoolean("is_fresh_install", true)
                .also { if (it) getSharedPreferences("analytics_prefs", MODE_PRIVATE).edit().putBoolean("is_fresh_install", false).apply() }

        analytics.track(AnalyticsEvent.GlobalEvent.AppOpened(
            platform = deviceInfo.platform,
            osVersion = deviceInfo.osVersion,
            appVersion = deviceInfo.appVersion,
            buildNumber = deviceInfo.buildNumber,
            isFreshInstall = isFreshInstall
        ))

        setContent {
            App()
        }
    }

    override fun onStart() {
        super.onStart()
        val userRole = RoleManager.currentRole.value?.name ?: ""
        CoroutineScope(Dispatchers.Main).launch {
            val userId = authRepository.getBusinessId() ?: ""
            analytics.track(AnalyticsEvent.GlobalEvent.SessionStarted(
                userRole = userRole,
                userId = userId,
                businessId = userId,
                isReturningUser = userId.isNotBlank()
            ))
        }
    }

    override fun onStop() {
        super.onStop()
        analytics.track(AnalyticsEvent.GlobalEvent.SessionEnded)
    }

    override fun onPause() {
        super.onPause()
        analytics.track(AnalyticsEvent.GlobalEvent.AppBackgrounded)
    }

    // Firebase Analytics writes events to SQLite before sending, so the event survives
    // the process termination and is uploaded on the next app launch.
    private fun installCrashHandler() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            analytics.track(AnalyticsEvent.GlobalEvent.AppCrashed)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
