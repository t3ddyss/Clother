package com.t3ddyss.clother.di.offer

import com.t3ddyss.clother.data.ImagesRepositoryImpl
import com.t3ddyss.clother.domain.offer.ImagesInteractor
import com.t3ddyss.clother.domain.offer.ImagesInteractorImpl
import com.t3ddyss.clother.domain.offer.ImagesRepository
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
    fun ImagesInteractorImpl.bindImagesInteractor(): ImagesInteractor

    @Singleton
    @Binds
    fun ImagesRepositoryImpl.bindImagesRepository(): ImagesRepository
}