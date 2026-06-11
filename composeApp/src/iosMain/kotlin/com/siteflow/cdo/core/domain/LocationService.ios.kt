package com.siteflow.cdo.core.domain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class LocationService {

    private val locationManager = CLLocationManager()
    private var delegate: CLLocationManagerDelegateProtocol? = null

    actual fun getCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // ✅ Check if system-level location services are ON
        if (!CLLocationManager.locationServicesEnabled()) {
            onFailure("LOCATION_SERVICES_DISABLED")
            return
        }

        // Single guard: ensures onSuccess/onFailure is called exactly once
        var didResolve = false
        fun resolveSuccess(lat: Double, lng: Double) {
            if (didResolve) return
            didResolve = true
            locationManager.stopUpdatingLocation()
            onSuccess(lat, lng)
        }
        fun resolveFailure(msg: String) {
            if (didResolve) return
            didResolve = true
            locationManager.stopUpdatingLocation()
            onFailure(msg)
        }

        delegate = object : NSObject(), CLLocationManagerDelegateProtocol {

            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>
            ) {
                val location = didUpdateLocations.firstOrNull() as? CLLocation
                if (location != null) {
                    location.coordinate.useContents {
                        resolveSuccess(latitude, longitude)
                    }
                } else {
                    resolveFailure("Unable to fetch location")
                }
            }

            override fun locationManager(
                manager: CLLocationManager,
                didFailWithError: NSError
            ) {
                resolveFailure("LOCATION_FETCH_FAILED")
            }

            override fun locationManagerDidChangeAuthorization(
                manager: CLLocationManager
            ) {
                if (didResolve) return

                when (manager.authorizationStatus) {
                    kCLAuthorizationStatusAuthorizedAlways,
                    kCLAuthorizationStatusAuthorizedWhenInUse -> {
                        manager.startUpdatingLocation()
                    }

                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        resolveFailure("LOCATION_PERMISSION_PERMANENTLY_DENIED")
                    }

                    else -> Unit
                }
            }
        }

        locationManager.delegate = delegate

        // NOTE: locationManagerDidChangeAuthorization fires when the delegate is set,
        // which may also call startUpdatingLocation(). The didResolve guard in
        // resolveSuccess/resolveFailure ensures we only invoke the callback once,
        // so calling startUpdatingLocation() from both paths is safe.
        when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()
            }

            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                locationManager.startUpdatingLocation()
            }

            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                resolveFailure("LOCATION_PERMISSION_PERMANENTLY_DENIED")
            }

            else -> Unit
        }
    }
}
