package com.t3ddyss.feature_location.di

import com.t3ddyss.feature_location.data.LocationRepositoryImpl
import com.t3ddyss.feature_location.domain.LocationInteractor
import com.t3ddyss.feature_location.domain.LocationInteractorImpl
import com.t3ddyss.feature_location.domain.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LocationModule {
    @Singleton
    @Binds
    fun bindLocationRepository(locationRepositoryImpl: LocationRepositoryImpl): LocationRepository

    @Singleton
    @Binds
    fun bindLocationInteractor(locationInteractorImpl: LocationInteractorImpl): LocationInteractor
}