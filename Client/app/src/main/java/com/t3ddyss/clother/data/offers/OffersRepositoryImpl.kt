package com.t3ddyss.clother.data.offers

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import arrow.core.Either
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.t3ddyss.clother.data.common.common.Mappers.toApiCallError
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.offers.db.CategoryDao
import com.t3ddyss.clother.data.offers.db.OfferDao
import com.t3ddyss.clother.data.offers.remote.RemoteOffersService
import com.t3ddyss.clother.domain.offers.ImagesRepository
import com.t3ddyss.clother.domain.offers.OffersRepository
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.ApiCallError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class OffersRepositoryImpl @Inject constructor(
    private val imagesRepository: ImagesRepository,
    private val service: RemoteOffersService,
    private val storage: Storage,
    private val offerDao: OfferDao,
    private val categoryDao: CategoryDao,
    private val offersRemoteMediatorFactory: OffersRemoteMediatorFactory,
    private val offersPagingSourceFactory: OffersPagingSourceFactory
) : OffersRepository {

    private val pagingConfig by lazy {
        PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = true // Workaround for https://issuetracker.google.com/issues/235319241
        )
    }

    override fun observeOffersFromDatabase(
        query: Map<String, String>,
        userId: Int?
    ): Flow<PagingData<Offer>> {
        val listKey = LIST_KEY + (userId ?: "")
        val remoteMediator = offersRemoteMediatorFactory.create(listKey, query)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { offerDao.getAllOffersByList(listKey) }
        ).flow
            .map {
                it.map { offerEntity -> offerEntity.toDomain() }
            }
    }

    override fun observeOffersFromNetwork(query: Map<String, String>): Flow<PagingData<Offer>> {
        val pagingSource = offersPagingSourceFactory.create(query)
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = { pagingSource }
        ).flow
            .map {
                it.map { offerDto -> offerDto.toDomain() }
            }
    }

    override suspend fun getOffer(id: Int): Offer {
        return offerDao.getOfferById(id).toDomain()
    }

    override suspend fun postOffer(
        title: String,
        categoryId: Int,
        description: String,
        images: List<Uri>,
        size: String?,
        location: LatLng?
    ): Either<ApiCallError, Offer> {
        val body = JsonObject().apply {
            addProperty("title", title)
            addProperty("category_id", categoryId)

            if (description.isNotEmpty()) {
                addProperty("description", description)
            }

            if (location != null) {
                addProperty("location", "${location.latitude},${location.longitude}")
            }

            if (size != null) {
                addProperty("size", size)
            }
        }
            .toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val parts = coroutineScope {
            images
                .map {
                    async {
                        imagesRepository.getCompressedImage(it)
                    }
                }
                .awaitAll()
                .map {
                    MultipartBody.Part.createFormData(
                        name = "file",
                        it.name,
                        it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                }
        }

        return service.postOffer(
            storage.accessToken,
            body,
            parts
        )
            .map { it.toDomain() }
            .mapLeft { it.toApiCallError() }
    }

    override suspend fun deleteOffer(offerId: Int): Either<ApiCallError, Unit> {
        return service.deleteOffer(
            storage.accessToken,
            offerId
        )
            .tap { offerDao.deleteOfferById(offerId) }
            .mapLeft { it.toApiCallError() }
    }

    override suspend fun getOfferCategories(parentCategoryId: Int?): List<Category> {
        return categoryDao.getCategories(parentCategoryId)
            .map { it.toDomain() }
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val LIST_KEY = "offers"
    }
}