package com.trackerx.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GpsState(
    val permissionGranted: Boolean = false,
    val providerEnabled: Boolean = false,
    val hasFix: Boolean = false,
    /** null enquanto não houver leitura válida do GPS. */
    val speedKmh: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

class LocationRepository(private val context: Context) {
    private val _state = MutableStateFlow(GpsState())
    val state: StateFlow<GpsState> = _state
    private val manager = context.getSystemService(LocationManager::class.java)
    private var started = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _state.value = _state.value.copy(
                providerEnabled = true,
                hasFix = true,
                speedKmh = if (location.hasSpeed()) location.speed * 3.6 else null,
                latitude = location.latitude,
                longitude = location.longitude
            )
        }

        override fun onProviderEnabled(provider: String) {
            _state.value = _state.value.copy(providerEnabled = true)
        }

        override fun onProviderDisabled(provider: String) {
            _state.value = _state.value.copy(providerEnabled = false, hasFix = false, speedKmh = null)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    @SuppressLint("MissingPermission")
    fun start() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted || manager == null) {
            _state.value = _state.value.copy(permissionGranted = granted)
            return
        }
        val enabled = runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }
            .getOrDefault(false)
        _state.value = _state.value.copy(permissionGranted = true, providerEnabled = enabled)
        if (started) return
        runCatching {
            manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000L, 0f, listener, Looper.getMainLooper()
            )
            started = true
        }
    }
}
