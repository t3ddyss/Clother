package com.t3ddyss.clother.domain.offers

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ImagesInteractorImpl @Inject constructor(
    private val imagesRepository: ImagesRepository
) : ImagesInteractor {
    override suspend fun observeLocalImages(): Flow<List<Uri>> {
        return imagesRepository.observeImages()
    }

    override suspend fun compressImage(uri: String): File = withContext(Dispatchers.IO) {
        imagesRepository.getCompressedImage(uri)
    }
}