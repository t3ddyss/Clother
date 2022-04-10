package com.t3ddyss.clother.di.common

import com.t3ddyss.clother.data.common.MapRepositoryImpl
import com.t3ddyss.clother.domain.common.MapRepository
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