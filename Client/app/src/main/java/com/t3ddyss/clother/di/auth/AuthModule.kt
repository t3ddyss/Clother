package com.t3ddyss.clother.di.auth

import com.t3ddyss.clother.data.auth.AuthRepositoryImpl
import com.t3ddyss.clother.data.auth.AuthTokenRepositoryImpl
import com.t3ddyss.clother.data.auth.ProfileRepositoryImpl
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.AuthInteractorImpl
import com.t3ddyss.clother.domain.auth.AuthRepository
import com.t3ddyss.clother.domain.auth.AuthTokenRepository
import com.t3ddyss.clother.domain.auth.ProfileInteractor
import com.t3ddyss.clother.domain.auth.ProfileInteractorImpl
import com.t3ddyss.clother.domain.auth.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Authenticator

@Module
@InstallIn(SingletonComponent::class)
interface AuthModule {
    @Singleton
    @Binds
    fun bindAuthInteractor(authInteractorImpl: AuthInteractorImpl): AuthInteractor

    @Singleton
    @Binds
    fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Singleton
    @Binds
    fun bindAuthTokenRepository(authTokenRepositoryImpl: AuthTokenRepositoryImpl): AuthTokenRepository

    @Singleton
    @Binds
    fun bindAuthenticator(authTokenRepositoryImpl: AuthTokenRepositoryImpl): Authenticator

    @Singleton
    @Binds
    fun bindProfileInteractor(profileInteractorImpl: ProfileInteractorImpl): ProfileInteractor

    @Singleton
    @Binds
    fun bindProfileRepository(profileRepositoryImpl: ProfileRepositoryImpl): ProfileRepository
}