package com.t3ddyss.clother.di.common

import com.t3ddyss.clother.util.DispatchersProvider
import com.t3ddyss.clother.util.DispatchersProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CoroutineModule {
    @Singleton
    @Binds
    fun DispatchersProviderImpl.bindDispatchersProvider(): DispatchersProvider

    companion object {
        @Singleton
        @Provides
        fun provideCoroutineScope() =
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
}