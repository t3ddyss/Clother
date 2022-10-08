package com.t3ddyss.clother.domain.offers

import android.net.Uri
import androidx.paging.PagingData
import com.google.gson.JsonObject
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import kotlinx.coroutines.flow.Flow

interface OffersRepository {
    fun observeOffersFromDatabase(
        query: Map<String, String>,
        userId: Int?
    ): Flow<PagingData<Offer>>

    fun observeOffersFromNetwork(
        query: Map<String, String> = emptyMap()
    ): Flow<PagingData<Offer>>

    suspend fun postOffer(offer: JsonObject, images: List<Uri>): Int

    suspend fun deleteOffer(offerId: Int)

    suspend fun getOfferCategories(parentCategoryId: Int?): List<Category>
}