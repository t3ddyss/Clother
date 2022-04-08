package com.t3ddyss.clother.domain.offer

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ImagesInteractor {
    suspend fun observeImages(): Flow<List<Uri>>
}