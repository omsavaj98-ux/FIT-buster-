package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GpsTrackingState(
    val isTracking: Boolean = false,
    val hasGpsFix: Boolean = false,
    val distanceMeters: Double = 0.0,
    val pointCount: Int = 0,
    val currentSpeedMps: Float = 0f,
    val accuracyMeters: Float = 0f
)

class LocationTracker(context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _state = MutableStateFlow(GpsTrackingState())
    val state: StateFlow<GpsTrackingState> = _state.asStateFlow()

    private var lastValidLocation: Location? = null
    private var activeCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startTracking() {
        stopTracking()
        lastValidLocation = null
        _state.value = GpsTrackingState(isTracking = true)

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .setMinUpdateDistanceMeters(2f)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    if (loc.accuracy > 50f) continue
                    val prev = lastValidLocation
                    var addedDistance = 0.0
                    if (prev != null) {
                        val delta = prev.distanceTo(loc).toDouble()
                        if (delta in 1.5..250.0) {
                            addedDistance = delta
                        }
                    }
                    lastValidLocation = loc
                    val current = _state.value
                    _state.value = current.copy(
                        hasGpsFix = true,
                        distanceMeters = current.distanceMeters + addedDistance,
                        pointCount = current.pointCount + 1,
                        currentSpeedMps = loc.speed,
                        accuracyMeters = loc.accuracy
                    )
                }
            }
        }
        activeCallback = callback
        try {
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (_: SecurityException) {
            _state.value = GpsTrackingState(isTracking = false)
        }
    }

    fun reset() {
        stopTracking()
        lastValidLocation = null
        _state.value = GpsTrackingState()
    }

    fun stopTracking() {
        activeCallback?.let { cb ->
            fusedClient.removeLocationUpdates(cb)
        }
        activeCallback = null
        _state.value = _state.value.copy(isTracking = false)
    }
}
