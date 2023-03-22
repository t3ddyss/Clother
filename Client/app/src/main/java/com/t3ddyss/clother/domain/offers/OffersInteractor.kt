package com.t3ddyss.clother.domain.offers

import android.net.Uri
import androidx.paging.PagingData
import arrow.core.Either
import arrow.core.Nel
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.ApiCallError
import kotlinx.coroutines.flow.Flow

interface OffersInteractor {
    fun observeOffersFromDatabase(
        query: Map<String, String> = emptyMap(),
        userId: Int? = null
    ): Flow<PagingData<Offer>>

    fun observeOffersFromNetwork(
        query: Map<String, String> = emptyMap()
    ): Flow<PagingData<Offer>>

    suspend fun getOffer(id: Int): Offer

    suspend fun validateParameters(
        title: String,
        images: List<Uri>
    ): Either<Nel<OfferParam>, Unit>

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

    enum class OfferParam {
        TITLE,
        IMAGES
    }
}