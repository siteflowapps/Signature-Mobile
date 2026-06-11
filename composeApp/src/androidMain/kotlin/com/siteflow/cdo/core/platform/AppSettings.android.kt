package com.siteflow.cdo.core.platform

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.provider.Settings
import org.koin.core.context.GlobalContext   // ✅ THIS import

actual fun openAppSettings() {
    val context: Context =
        GlobalContext.get().get()   // ✅ NOTE: no `.koin`

    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}
