package com.siteflow.signature.core.domain

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LocationPermissionHelper(
    private val activity: ComponentActivity
) {

    private var onGranted: (() -> Unit)? = null
    private var onDenied: (() -> Unit)? = null

    private var permissionAskedOnce = false


    private val permissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) onGranted?.invoke()
            else onDenied?.invoke()
        }

    fun ensurePermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        this.onGranted = onGranted
        this.onDenied = onDenied

        val hasPermission =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            onGranted()
        } else {
            permissionAskedOnce = true
            permissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    fun isPermanentlyDenied(): Boolean {
        return permissionAskedOnce &&
                !hasPermission() &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
    }

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
