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
        // North America
        "new york"      to Pair(40.7, -74.0),
        "los angeles"   to Pair(34.1, -118.2),
        "chicago"       to Pair(41.9, -87.6),
        "houston"       to Pair(29.8, -95.4),
        "miami"         to Pair(25.8, -80.2),
        "toronto"       to Pair(43.7, -79.4),
        "vancouver"     to Pair(49.3, -123.1),
        "mexico city"   to Pair(19.4, -99.1),
        // Europe
        "london"        to Pair(51.5, -0.1),
        "paris"         to Pair(48.9, 2.3),
        "berlin"        to Pair(52.5, 13.4),
        "madrid"        to Pair(40.4, -3.7),
        "rome"          to Pair(41.9, 12.5),
        "amsterdam"     to Pair(52.4, 4.9),
        "brussels"      to Pair(50.8, 4.4),
        "vienna"        to Pair(48.2, 16.4),
        "zurich"        to Pair(47.4, 8.5),
        "stockholm"     to Pair(59.3, 18.1),
        "oslo"          to Pair(59.9, 10.8),
        "copenhagen"    to Pair(55.7, 12.6),
        "warsaw"        to Pair(52.2, 21.0),
        "lisbon"        to Pair(38.7, -9.1),
        "athens"        to Pair(37.9, 23.7),
        // Asia-Pacific
        "tokyo"         to Pair(35.7, 139.7),
        "beijing"       to Pair(39.9, 116.4),
        "shanghai"      to Pair(31.2, 121.5),
        "seoul"         to Pair(37.6, 127.0),
        "singapore"     to Pair(1.3, 103.8),
        "hong kong"     to Pair(22.3, 114.2),
        "bangkok"       to Pair(13.8, 100.5),
        "mumbai"        to Pair(19.1, 72.9),
        "delhi"         to Pair(28.6, 77.2),
        "dubai"         to Pair(25.2, 55.3),
        "sydney"        to Pair(-33.9, 151.2),
        "melbourne"     to Pair(-37.8, 145.0),
        // South America & Africa
        "sao paulo"     to Pair(-23.5, -46.6),
        "buenos aires"  to Pair(-34.6, -58.4),
        "cairo"         to Pair(30.1, 31.2),
        "johannesburg"  to Pair(-26.2, 28.0),
        "nairobi"       to Pair(-1.3, 36.8)
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
