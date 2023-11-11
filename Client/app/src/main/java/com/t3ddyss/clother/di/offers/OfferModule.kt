package com.t3ddyss.clother.di.offers

import com.t3ddyss.clother.data.offers.ImagesRepositoryImpl
import com.t3ddyss.clother.data.offers.OffersRepositoryImpl
import com.t3ddyss.clother.domain.offers.ImagesInteractor
import com.t3ddyss.clother.domain.offers.ImagesInteractorImpl
import com.t3ddyss.clother.domain.offers.ImagesRepository
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.OffersInteractorImpl
import com.t3ddyss.clother.domain.offers.OffersRepository
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
    fun bindOffersInteractor(offersInteractorImpl: OffersInteractorImpl): OffersInteractor

    @Singleton
    @Binds
    fun bindOffersRepository(offersRepositoryImpl: OffersRepositoryImpl): OffersRepository

    @Singleton
    @Binds
    fun bindImagesInteractor(imagesInteractorImpl: ImagesInteractorImpl): ImagesInteractor

    @Singleton
    @Binds
    fun bindImagesRepository(imagesRepositoryImpl: ImagesRepositoryImpl): ImagesRepository
}