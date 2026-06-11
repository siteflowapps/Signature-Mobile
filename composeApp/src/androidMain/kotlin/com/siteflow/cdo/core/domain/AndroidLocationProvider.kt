package com.siteflow.cdo.core.domain


class AndroidLocationProvider(
    private val permissionHelper: LocationPermissionHelper,
    private val locationService: LocationService
) {

    fun getLocation(
        onSuccess: (Double, Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        when {
            permissionHelper.hasPermission() -> {
                locationService.fetchLocation(onSuccess, onFailure)
            }

            permissionHelper.isPermanentlyDenied() -> {
                onFailure("LOCATION_PERMISSION_PERMANENTLY_DENIED")
            }

            else -> {
                permissionHelper.ensurePermission(
                    onGranted = {
                        locationService.fetchLocation(onSuccess, onFailure)
                    },
                    onDenied = {
                        // 🔑 retryable deny
                        onFailure("LOCATION_PERMISSION_DENIED")
                    }
                )
            }
        }
    }
}
