package com.t3ddyss.clother.domain.offer

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImagesInteractorImpl @Inject constructor(
    private val imagesRepository: ImagesRepository
) : ImagesInteractor {
    override suspend fun observeImages(): Flow<List<Uri>> {
        return imagesRepository.observeImages()
    }
}