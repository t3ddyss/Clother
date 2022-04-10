package com.t3ddyss.clother.di.offers

import com.t3ddyss.clother.data.offers.ImagesRepositoryImpl
import com.t3ddyss.clother.data.offers.OffersRepositoryImpl
import com.t3ddyss.clother.domain.offers.*
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