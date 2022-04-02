package com.t3ddyss.clother.di.auth

import com.t3ddyss.clother.data.auth.AuthRepositoryImpl
import com.t3ddyss.clother.data.auth.AuthTokenRepositoryImpl
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.AuthInteractorImpl
import com.t3ddyss.clother.domain.auth.AuthRepository
import com.t3ddyss.clother.domain.auth.AuthTokenRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AuthModule {
    @Singleton
    @Binds
    fun AuthInteractorImpl.bindAuthInteractor(): AuthInteractor

    @Singleton
    @Binds
    fun AuthRepositoryImpl.bindAuthRepository(): AuthRepository

    @Singleton
    @Binds
    fun AuthTokenRepositoryImpl.bindAuthTokenRepository(): AuthTokenRepository

    @Singleton
    @Binds
    fun AuthTokenRepositoryImpl.bindAuthenticator(): Authenticator
}