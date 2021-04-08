package com.t3ddyss.clother.data

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.t3ddyss.clother.models.LatLngWrapper
import com.t3ddyss.clother.utilities.DEBUG_TAG
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@ExperimentalCoroutinesApi
class LocationProvider @Inject constructor(
        application: Application
) {
    private val locationProviderClient = LocationServices
            .getFusedLocationProviderClient(application.applicationContext)

    suspend fun getLocation(): Flow<LatLngWrapper> = merge(getInitalLocation(), getLocationUpdates())

    @SuppressLint("MissingPermission")
    private fun getInitalLocation() = callbackFlow {

        val initialLocationListener = OnSuccessListener<Location?> {
            Log.d(DEBUG_TAG, "Got location in last location $it")

            if (it != null) {
                val latLng = LatLngWrapper(
                    latLng = LatLng(it.latitude, it.longitude),
                    isInitialValue = false,
                    isManuallySelected = false
                )

                offer(latLng)
            }
        }
        locationProviderClient.lastLocation.addOnSuccessListener(initialLocationListener)

        awaitClose()
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocationUpdates() = callbackFlow<LatLngWrapper>{
        val locationRequest = LocationRequest.create()
        locationRequest.interval =  5_000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationUpdatesObserver = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d(DEBUG_TAG, "Got location in onLocationResult $locationResult")

                if (locationResult != null) {
                    val location = locationResult.locations.lastOrNull()

                    if (location != null) {
                        val latLng = LatLngWrapper(
                                latLng = LatLng(location.latitude, location.longitude),
                                isInitialValue = false,
                                isManuallySelected = false
                        )

                        offer(latLng)
                    }
                }
            }
        }

        locationProviderClient.requestLocationUpdates(
                locationRequest,
                locationUpdatesObserver,
                Looper.getMainLooper())

        awaitClose {
            locationProviderClient.removeLocationUpdates(locationUpdatesObserver)
        }
    }
}