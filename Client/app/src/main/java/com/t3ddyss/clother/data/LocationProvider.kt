package com.t3ddyss.clother.data

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.t3ddyss.clother.db.LocationDao
import com.t3ddyss.clother.models.domain.LocationData
import com.t3ddyss.clother.models.entity.LocationEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

class LocationProvider @Inject constructor(
    application: Application,
    private val locationDao: LocationDao
) {
    private val locationProviderClient = LocationServices
        .getFusedLocationProviderClient(application.applicationContext)

    suspend fun observeLocation() = merge(getInitialLocation(), observeLocationUpdates())

    suspend fun getLatestSavedLocation() = locationDao.getLatestLocation()

    suspend fun saveSelectedLocation(location: LocationData) {
        locationDao.insert(
            (LocationEntity(
                lat = location.latLng.latitude,
                lng = location.latLng.longitude
            ))
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun getInitialLocation() = callbackFlow {
        val initialLocationListener = OnSuccessListener<Location> {
            val latLng = LocationData(
                latLng = LatLng(it.latitude, it.longitude),
                isInitialValue = false,
                isManuallySelected = false
            )

            trySend(latLng)
        }
        locationProviderClient.lastLocation.addOnSuccessListener(initialLocationListener)

        awaitClose()
    }

    @SuppressLint("MissingPermission")
    private suspend fun observeLocationUpdates() = callbackFlow {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5_000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val locationUpdatesObserver = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null) {
                    val location = locationResult.locations.lastOrNull()

                    if (location != null) {
                        val latLng = LocationData(
                            latLng = LatLng(location.latitude, location.longitude),
                            isInitialValue = false,
                            isManuallySelected = false
                        )

                        trySend(latLng)
                    }
                }
            }
        }

        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationUpdatesObserver,
            Looper.getMainLooper()
        )

        awaitClose {
            locationProviderClient.removeLocationUpdates(locationUpdatesObserver)
        }
    }
}