package com.t3ddyss.clother.di.auth

import com.t3ddyss.clother.data.auth.AuthRepositoryImpl
import com.t3ddyss.clother.data.auth.AuthTokenRepositoryImpl
import com.t3ddyss.clother.domain.auth.*
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

    @Singleton
    @Binds
    fun ProfileInteractorImpl.bindProfileInteractor(): ProfileInteractor
}