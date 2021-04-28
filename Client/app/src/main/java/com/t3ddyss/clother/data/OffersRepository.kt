package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.net.Uri
import androidx.paging.*
import com.google.gson.JsonObject
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.CategoryDao
import com.t3ddyss.clother.db.OfferDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.domain.Offer
import com.t3ddyss.clother.models.domain.Resource
import com.t3ddyss.clother.models.domain.Response
import com.t3ddyss.clother.models.domain.Success
import com.t3ddyss.clother.models.mappers.mapCategoryEntityToDomain
import com.t3ddyss.clother.models.mappers.mapOfferEntityToDomain
import com.t3ddyss.clother.models.mappers.mapResponseDtoToDomain
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.CLOTHER_PAGE_SIZE
import com.t3ddyss.clother.utilities.handleNetworkException
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

class OffersRepository
@Inject constructor(
    private val service: ClotherOffersService,
    private val imageProvider: ImageProvider,
    private val prefs: SharedPreferences,
    private val db: AppDatabase,
    private val offerDao: OfferDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val categoryDao: CategoryDao
) {
    /**
     * Gets offers and saves them in database
     */
    @ExperimentalPagingApi
    fun getOffers(query: Map<String, String> = mapOf(), userId: Int? = null):
            Flow<PagingData<Offer>> {
        val listKey = LIST_KEY_OFFERS + (userId ?: "")
        return Pager(
            config = PagingConfig(
                pageSize = CLOTHER_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = OffersRemoteMediator(
                query = query,
                service = service,
                prefs = prefs,
                db = db,
                offerDao = offerDao,
                remoteKeyDao = remoteKeyDao,
                listKey = listKey
            ),
            pagingSourceFactory = {
                offerDao.getAllOffersByList(listKey)
            }
        ).flow
            .map {
                it.map { offerEntity ->
                    mapOfferEntityToDomain(offerEntity)
                }
            }
    }

    /**
     * Gets offers without saving them in database
     */
    fun getOffers(query: Map<String, String>): Flow<PagingData<Offer>> {
        return Pager(
            config = PagingConfig(
                pageSize = CLOTHER_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                OffersPagingSource(
                    service,
                    prefs,
                    query
                )
            }
        ).flow
    }

    suspend fun getCategories(parentId: Int? = null) =
        categoryDao.getSubcategories(parentId)
            .map { mapCategoryEntityToDomain(it) }

    suspend fun postOffer(offer: JsonObject, images: List<Uri>): Resource<Int> {
        val body = offer.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val imageFiles = images.map {
            coroutineScope {
                async {
                    imageProvider.getCompressedImageFile(it)
                }
            }
        }.awaitAll().map {
            MultipartBody.Part.createFormData(
                name = "file",
                it.name,
                it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
        }

        return handleNetworkException {
            val response = service.postOffer(
                prefs.getString(ACCESS_TOKEN, null),
                body,
                imageFiles
            )
            Success(response.id)
        }
    }

    suspend fun deleteOffer(offerEntity: Offer): Resource<*> {
        return handleNetworkException {
            service.deleteOffer(
                prefs.getString(ACCESS_TOKEN, null),
                offerEntity.id
            )
            offerDao.deleteOfferById(offerEntity.id)

            Success(null)
        }
    }

    companion object {
        const val LIST_KEY_OFFERS = "offers"
    }
}