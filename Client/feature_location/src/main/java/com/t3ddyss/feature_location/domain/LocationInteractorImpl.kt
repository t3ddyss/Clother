package com.t3ddyss.feature_location.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationInteractorImpl @Inject constructor(
    private val repository: LocationRepository
) : LocationInteractor {

    override suspend fun observeLocation(): Flow<LocationData> = repository.observeLocation()

    override suspend fun getLastSavedLocationOrNull(): LocationData? = repository.getLastLocation()

    override suspend fun saveLocationIfNeeded(location: LocationData) {
        if (location.locationType != LocationType.INITIAL) {
            repository.saveLocation(location)
        }
    }
}