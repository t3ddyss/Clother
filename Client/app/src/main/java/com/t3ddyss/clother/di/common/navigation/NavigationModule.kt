package com.t3ddyss.clother.di.common.navigation

import com.t3ddyss.clother.data.common.navigation.NavigationRepositoryImpl
import com.t3ddyss.clother.domain.common.navigation.NavigationInteractor
import com.t3ddyss.clother.domain.common.navigation.NavigationInteractorImpl
import com.t3ddyss.clother.domain.common.navigation.NavigationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NavigationModule {
    @Singleton
    @Binds
    fun NavigationInteractorImpl.bindNavigationInteractor(): NavigationInteractor

    @Singleton
    @Binds
    fun NavigationRepositoryImpl.bindNavigationRepository(): NavigationRepository
}