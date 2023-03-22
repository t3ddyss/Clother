package com.t3ddyss.clother.domain.offers

import android.net.Uri
import androidx.paging.PagingData
import arrow.core.Either
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.ApiCallError
import kotlinx.coroutines.flow.Flow

interface OffersRepository {
    fun observeOffersFromDatabase(
        query: Map<String, String>,
        userId: Int?
    ): Flow<PagingData<Offer>>

    fun observeOffersFromNetwork(
        query: Map<String, String> = emptyMap()
    ): Flow<PagingData<Offer>>

    suspend fun getOffer(id: Int): Offer

    suspend fun postOffer(
        title: String,
        categoryId: Int,
        description: String,
        images: List<Uri>,
        size: String?,
        location: LatLng?
    ): Either<ApiCallError, Offer>

    suspend fun deleteOffer(offerId: Int): Either<ApiCallError, Unit>

    suspend fun getOfferCategories(parentCategoryId: Int?): List<Category>
}