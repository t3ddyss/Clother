package com.t3ddyss.clother.domain.offer

import androidx.paging.PagingData
import com.google.gson.JsonObject
import com.t3ddyss.clother.domain.models.Offer
import com.t3ddyss.core.domain.models.Category
import kotlinx.coroutines.flow.Flow
import java.io.File

interface OffersRepository {
    fun observeOffersFromDatabase(
        query: Map<String, String>,
        userId: Int?
    ): Flow<PagingData<Offer>>

    fun observeOffersFromNetwork(
        query: Map<String, String> = emptyMap()
    ): Flow<PagingData<Offer>>

    suspend fun postOffer(offer: JsonObject, images: List<File>): Int

    suspend fun deleteOffer(offerId: Int)

    suspend fun getOfferCategories(parentCategoryId: Int?): List<Category>
}