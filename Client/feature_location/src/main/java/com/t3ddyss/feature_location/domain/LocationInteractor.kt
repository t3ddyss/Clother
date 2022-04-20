package com.t3ddyss.feature_location.domain

import com.t3ddyss.feature_location.domain.models.LocationData
import kotlinx.coroutines.flow.Flow

interface LocationInteractor {
    suspend fun observeLocation(): Flow<LocationData>
    suspend fun getLastSavedLocationOrNull(): LocationData?
    suspend fun saveLocationIfNeeded(location: LocationData)
}