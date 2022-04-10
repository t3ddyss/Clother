package com.t3ddyss.clother.di.common

import com.t3ddyss.clother.domain.common.InitializationInteractor
import com.t3ddyss.clother.domain.common.InitializationInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CommonModule {
    @Singleton
    @Binds
    fun InitializationInteractorImpl.bindInitializationInteractor(): InitializationInteractor
}