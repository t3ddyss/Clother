package com.t3ddyss.clother.domain.offers

import android.net.Uri
import androidx.paging.PagingData
import arrow.core.Either
import arrow.core.Nel
import arrow.core.invalidNel
import arrow.core.traverse
import arrow.core.validNel
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.ApiCallError
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class OffersInteractorImpl @Inject constructor(
    private val offersRepository: OffersRepository
) : OffersInteractor {

    override fun observeOffersFromDatabase(
        query: Map<String, String>,
        userId: Int?
    ): Flow<PagingData<Offer>> {
        return offersRepository.observeOffersFromDatabase(query, userId)
    }

    override fun observeOffersFromNetwork(query: Map<String, String>): Flow<PagingData<Offer>> {
        return offersRepository.observeOffersFromNetwork(query)
    }

    override suspend fun getOffer(id: Int): Offer {
        return offersRepository.getOffer(id)
    }

    override suspend fun validateParameters(
        title: String,
        images: List<Uri>
    ): Either<Nel<OffersInteractor.OfferParam>, Unit> {
        return OffersInteractor.OfferParam.values().asList().traverse { param ->
            when (param) {
                OffersInteractor.OfferParam.TITLE -> {
                    if (title.isNotBlank()) {
                        param.validNel()
                    } else {
                        param.invalidNel()
                    }
                }
                OffersInteractor.OfferParam.IMAGES -> {
                    if (images.isNotEmpty()) {
                        param.validNel()
                    } else {
                        param.invalidNel()
                    }
                }
            }
        }
            .toEither()
            .void()
    }

    override suspend fun postOffer(
        title: String,
        categoryId: Int,
        description: String,
        images: List<Uri>,
        size: String?,
        location: LatLng?
    ): Either<ApiCallError, Offer> {
        require(validateParameters(title, images).isRight())
        return offersRepository.postOffer(
            title = title,
            categoryId = categoryId,
            description = description,
            images = images,
            size = size,
            location = location
        )
    }

    override suspend fun deleteOffer(offerId: Int): Either<ApiCallError, Unit> {
        return offersRepository.deleteOffer(offerId)
    }

    override suspend fun getOfferCategories(parentCategoryId: Int?): List<Category> {
        return offersRepository.getOfferCategories(parentCategoryId)
    }
}