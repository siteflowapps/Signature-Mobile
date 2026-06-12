package com.siteflow.signature.core.domain


import android.content.Context
import com.google.android.gms.location.LocationServices

actual class LocationService(
    context: Context,
    permissionHelper: LocationPermissionHelper
) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val provider =
        AndroidLocationProvider(permissionHelper, this)

    actual fun getCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        provider.getLocation(onSuccess, onFailure)
    }

    // 🔑 INTERNAL: called ONLY after permission is granted
    internal fun fetchLocation(
        onSuccess: (Double, Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onSuccess(location.latitude, location.longitude)
                    } else {
                        onFailure("Unable to fetch location. Enable GPS.")
                    }
                }
                .addOnFailureListener {
                    onFailure("Failed to get location")
                }
        } catch (e: SecurityException) {
            // ✅ Handles edge cases + silences lint
            onFailure("Location permission not granted")
        }
    }

}

