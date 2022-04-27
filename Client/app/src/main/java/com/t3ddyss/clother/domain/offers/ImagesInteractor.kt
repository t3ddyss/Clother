package com.t3ddyss.clother.domain.offers

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ImagesInteractor {
    suspend fun observeLocalImages(): Flow<List<Uri>>
    suspend fun compressImage(uri: String): File
}