package com.t3ddyss.clother.domain.offers

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ImagesRepository {
    suspend fun observeImages(): Flow<List<Uri>>
    suspend fun getCompressedImage(uri: String): File
}