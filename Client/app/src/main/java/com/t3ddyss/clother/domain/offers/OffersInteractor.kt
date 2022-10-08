package com.t3ddyss.clother.domain.offers

import android.net.Uri
import androidx.paging.PagingData
import com.google.gson.JsonObject
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.Resource
import kotlinx.coroutines.flow.Flow

interface OffersInteractor {
    fun observeOffersFromDatabase(
        query: Map<String, String> = emptyMap(),
        userId: Int? = null
    ): Flow<PagingData<Offer>>

    fun observeOffersFromNetwork(
        query: Map<String, String> = emptyMap()
    ): Flow<PagingData<Offer>>

    suspend fun postOffer(offer: JsonObject, images: List<Uri>): Resource<Int>

    suspend fun deleteOffer(offerId: Int): Resource<*>

    suspend fun getOfferCategories(parentCategoryId: Int?): List<Category>
}