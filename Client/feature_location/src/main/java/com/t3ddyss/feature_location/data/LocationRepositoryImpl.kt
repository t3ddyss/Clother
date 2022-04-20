package com.t3ddyss.feature_location.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.t3ddyss.feature_location.domain.LocationRepository
import com.t3ddyss.feature_location.domain.models.LocationData
import com.t3ddyss.feature_location.domain.models.LocationType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    @ApplicationContext
    context: Context
) : LocationRepository {
    private val locationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override suspend fun getLastLocation() = locationDao.getLatestLocation()?.toDomain()

    override suspend fun saveLocation(location: LocationData) {
        locationDao.insert(location.toEntity())
    }

    @SuppressLint("MissingPermission")
    override suspend fun observeLocation() = callbackFlow {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5_000
        locationRequest.fastestInterval = 1_000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val initialLocationListener = OnSuccessListener<Location> {
            if (it != null) {
                val latLng = LocationData(
                    latLng = LatLng(it.latitude, it.longitude),
                    locationType = LocationType.DETECTED_BY_GPS
                )
                trySend(latLng)
            }
        }
        val locationUpdatesObserver = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.locations.lastOrNull()

                if (location != null) {
                    val latLng = LocationData(
                        latLng = LatLng(location.latitude, location.longitude),
                        locationType = LocationType.DETECTED_BY_GPS
                    )

                    trySend(latLng)
                }
            }
        }

        locationProviderClient.lastLocation.addOnSuccessListener(initialLocationListener)
        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationUpdatesObserver,
            Looper.getMainLooper()
        )

        awaitClose {
            locationProviderClient.removeLocationUpdates(locationUpdatesObserver)
        }
    }

    private fun LocationEntity.toDomain(): LocationData {
        return LocationData(
            latLng = LatLng(this.lat, this.lng),
            locationType = LocationType.MANUALLY_SELECTED
        )
    }

    private fun LocationData.toEntity(): LocationEntity {
        return LocationEntity(
            lat = this.latLng.latitude,
            lng = this.latLng.longitude
        )
    }
}