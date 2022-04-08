package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.gson.JsonObject
import com.t3ddyss.clother.data.Mappers.toDomain
import com.t3ddyss.clother.data.db.AppDatabase
import com.t3ddyss.clother.data.db.CategoryDao
import com.t3ddyss.clother.data.db.OfferDao
import com.t3ddyss.clother.data.db.RemoteKeyDao
import com.t3ddyss.clother.data.remote.RemoteOffersService
import com.t3ddyss.clother.domain.models.Offer
import com.t3ddyss.clother.domain.offer.ImagesRepository
import com.t3ddyss.clother.util.ACCESS_TOKEN
import com.t3ddyss.clother.util.CLOTHER_PAGE_SIZE
import com.t3ddyss.clother.util.handleHttpException
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.Success
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
    private val service: RemoteOffersService,
    private val imagesRepository: ImagesRepository,
    private val prefs: SharedPreferences,
    private val db: AppDatabase,
    private val offerDao: OfferDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val categoryDao: CategoryDao
) {
    /**
     * Gets offers from API and stores them in database
     */
    fun observeOffers(query: Map<String, String> = mapOf(), userId: Int? = null):
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
                it.map { offerEntity -> offerEntity.toDomain() }
            }
    }

    /**
     * Gets offers without storing them in database
     */
    fun observeOffers(query: Map<String, String>): Flow<PagingData<Offer>> {
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
            .map { it.toDomain() }

    suspend fun postOffer(offer: JsonObject, images: List<Uri>): Resource<Int> {
        val body = offer.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val imageFiles = coroutineScope {
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

        return handleHttpException {
            val response = service.postOffer(
                prefs.getString(ACCESS_TOKEN, null),
                body,
                imageFiles
            )
            response.id
        }
    }

    suspend fun deleteOffer(offerEntity: Offer): Resource<*> {
        return handleHttpException {
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