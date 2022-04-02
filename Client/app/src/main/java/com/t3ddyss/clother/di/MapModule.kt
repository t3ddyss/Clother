package com.t3ddyss.clother.di

import com.t3ddyss.clother.data.MapRepositoryImpl
import com.t3ddyss.clother.domain.MapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface MapModule {
    @Binds
    fun MapRepositoryImpl.bindMapRepository(): MapRepository
}