package com.weatherapp.data.location

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.weatherapp.data.datastore.PreferenceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    private val cityCoordinates: Map<String, Pair<Double, Double>> = mapOf(
        "london"        to Pair(51.5, -0.1),
        "new york"      to Pair(40.7, -74.0),
        "sydney"        to Pair(-33.9, 151.2),
        "toronto"       to Pair(43.7, -79.4),
        "paris"         to Pair(48.9, 2.3),
        "berlin"        to Pair(52.5, 13.4)
    )

    suspend fun getSnappedLocation(): Pair<Double, Double>? {
        val permissionResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionResult != PermissionChecker.PERMISSION_GRANTED) {
            Timber.d("Location permission not granted — checking manual location fallback")
            return getManualLocationFallback()
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val provider = LocationManager.NETWORK_PROVIDER
            val location = locationManager.getLastKnownLocation(provider)
            if (location == null) {
                Timber.d("No last known location available — checking manual location fallback")
                return getManualLocationFallback()
            }
            // Raw GPS never leaves this function — snap before returning
            val snappedLat = location.latitude.snapToGrid()
            val snappedLon = location.longitude.snapToGrid()
            Pair(snappedLat, snappedLon)
        } catch (e: SecurityException) {
            Timber.w(e, "SecurityException when accessing location")
            getManualLocationFallback()
        }
    }

    private suspend fun getManualLocationFallback(): Pair<Double, Double>? {
        val prefs = dataStore.data.first()
        val manualLocation = prefs[PreferenceKeys.KEY_MANUAL_LOCATION]
        if (manualLocation == null) {
            Timber.d("No manual location set — returning null")
            return null
        }
        val key = manualLocation.trim().lowercase()
        val coords = cityCoordinates[key]
        if (coords == null) {
            Timber.w("Manual location '$manualLocation' not found in city lookup")
        } else {
            Timber.d("Using manual location '$manualLocation' -> $coords")
        }
        return coords
    }
}
