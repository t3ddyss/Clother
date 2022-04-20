package com.t3ddyss.clother.di.common.initialization

import com.t3ddyss.clother.data.common.initialization.MapRepositoryImpl
import com.t3ddyss.clother.domain.common.initialization.InitializationInteractor
import com.t3ddyss.clother.domain.common.initialization.InitializationInteractorImpl
import com.t3ddyss.clother.domain.common.initialization.MapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface InitializationModule {
    @Singleton
    @Binds
    fun InitializationInteractorImpl.bindInitializationInteractor(): InitializationInteractor

    @Binds
    fun MapRepositoryImpl.bindMapRepository(): MapRepository
}