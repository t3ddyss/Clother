package com.t3ddyss.feature_location.domain

import android.annotation.SuppressLint
import com.t3ddyss.feature_location.domain.models.LocationData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    @SuppressLint("MissingPermission")
    suspend fun observeLocation(): Flow<LocationData>

    suspend fun getLastLocation(): LocationData?

    suspend fun saveLocation(location: LocationData)
}