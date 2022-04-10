package com.t3ddyss.clother.di.offer

import com.t3ddyss.clother.data.offer.ImagesRepositoryImpl
import com.t3ddyss.clother.data.offer.OffersRepositoryImpl
import com.t3ddyss.clother.domain.offer.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface OfferModule {
    @Singleton
    @Binds
    fun OffersInteractorImpl.bindOffersInteractor(): OffersInteractor

    @Singleton
    @Binds
    fun OffersRepositoryImpl.bindOffersRepository(): OffersRepository

    @Singleton
    @Binds
    fun ImagesInteractorImpl.bindImagesInteractor(): ImagesInteractor

    @Singleton
    @Binds
    fun ImagesRepositoryImpl.bindImagesRepository(): ImagesRepository
}