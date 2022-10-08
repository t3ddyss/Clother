package com.t3ddyss.clother.domain.offers

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImagesInteractorImpl @Inject constructor(
    private val imagesRepository: ImagesRepository
) : ImagesInteractor {
    override suspend fun observeLocalImages(): Flow<List<Uri>> = imagesRepository.observeImages()
}