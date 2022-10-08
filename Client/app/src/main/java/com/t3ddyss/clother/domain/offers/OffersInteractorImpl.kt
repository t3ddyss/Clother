package com.t3ddyss.clother.domain.offers

import android.net.Uri
import androidx.paging.PagingData
import com.google.gson.JsonObject
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.util.handleHttpException
import com.t3ddyss.core.domain.models.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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

    override suspend fun postOffer(
        offer: JsonObject,
        images: List<Uri>
    ): Resource<Int> = handleHttpException {
        offersRepository.postOffer(offer, images)
    }

    override suspend fun deleteOffer(offerId: Int) = handleHttpException {
        offersRepository.deleteOffer(offerId)
    }

    override suspend fun getOfferCategories(parentCategoryId: Int?): List<Category> {
        return offersRepository.getOfferCategories(parentCategoryId)
    }
}