package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.net.Uri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.gson.JsonObject
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.db.*
import com.t3ddyss.clother.models.common.Error
import com.t3ddyss.clother.models.common.Failed
import com.t3ddyss.clother.models.common.Resource
import com.t3ddyss.clother.models.common.Success
import com.t3ddyss.clother.models.offers.NewOfferResponse
import com.t3ddyss.clother.models.offers.Offer
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.CLOTHER_PAGE_SIZE
import com.t3ddyss.clother.utilities.handleError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

@ExperimentalPagingApi
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
    fun getOffers(query: Map<String, String> = mapOf(), userId: Int? = null):
            Flow<PagingData<Offer>> {
        val listKey = LIST_KEY_OFFERS + (userId ?: "")
        return Pager(
                config = PagingConfig(
                        pageSize = CLOTHER_PAGE_SIZE,
                        enablePlaceholders = false),
                remoteMediator = OffersRemoteMediator(
                        query = query,
                        service = service,
                        prefs = prefs,
                        db = db,
                        offerDao = offerDao,
                        remoteKeyDao = remoteKeyDao,
                        listKey = listKey),
                pagingSourceFactory = { offerDao.getAllOffersByList(listKey) }
        ).flow
    }

    /**
     * Gets offers without saving them in database
     */
    fun getOffers(query: Map<String, String>): Flow<PagingData<Offer>> {
        return Pager(
                config = PagingConfig(
                        pageSize = CLOTHER_PAGE_SIZE,
                        enablePlaceholders = false),
                pagingSourceFactory = {
                    OffersPagingSource(service,
                            prefs,
                            query
                    )
                }
        ).flow
    }

    suspend fun getOfferById(id: Int) = offerDao.getOfferById(id)

    suspend fun getCategories(parentId: Int? = null) = categoryDao.getSubcategories(parentId)

    suspend fun postOffer(offer: JsonObject, images: List<Uri>): Resource<NewOfferResponse> {
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

        return try {
            val response = service.postOffer(
                    prefs.getString(ACCESS_TOKEN, null),
                    body,
                    imageFiles)
            Success(response)
        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }

    companion object {
        const val LIST_KEY_OFFERS = "offers"
    }
}