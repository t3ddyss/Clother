package com.t3ddyss.clother.data.offer

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.gson.JsonObject
import com.t3ddyss.clother.data.Mappers.toDomain
import com.t3ddyss.clother.data.Storage
import com.t3ddyss.clother.data.db.CategoryDao
import com.t3ddyss.clother.data.db.OfferDao
import com.t3ddyss.clother.data.remote.RemoteOffersService
import com.t3ddyss.clother.domain.models.Offer
import com.t3ddyss.clother.domain.offer.OffersRepository
import com.t3ddyss.core.domain.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class OffersRepositoryImpl @Inject constructor(
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
            enablePlaceholders = false
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

    override suspend fun postOffer(offer: JsonObject, images: List<File>): Int {
        val body = offer
            .toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val multipartBodyFiles = images.map {
            MultipartBody.Part.createFormData(
                name = "file",
                it.name,
                it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
        }

        return service.postOffer(
            storage.accessToken,
            body,
            multipartBodyFiles
        ).id
    }

    override suspend fun deleteOffer(offerId: Int) {
        service.deleteOffer(
            storage.accessToken,
            offerId
        )
        offerDao.deleteOfferById(offerId)
    }

    override suspend fun getOfferCategories(parentCategoryId: Int?): List<Category> {
        return categoryDao.getSubcategories(parentCategoryId)
            .map { it.toDomain() }
    }

    private companion object {
        const val PAGE_SIZE = 10
        const val LIST_KEY = "offers"
    }
}