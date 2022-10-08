package com.t3ddyss.clother.domain.offers

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface ImagesInteractor {
    suspend fun observeLocalImages(): Flow<List<Uri>>
}