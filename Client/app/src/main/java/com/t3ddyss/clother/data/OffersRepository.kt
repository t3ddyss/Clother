package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.net.Uri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.gson.JsonObject
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.CategoryDao
import com.t3ddyss.clother.db.OfferDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.models.NewOfferResponse
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
@ExperimentalCoroutinesApi
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

    fun getOffersStream(query: Map<String, String>): Flow<PagingData<Offer>> {
        val pagingSourceFactory = { offerDao.getAllOffers() }
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
                        remoteKeyDao = remoteKeyDao),
                pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    suspend fun getOfferById(id: Int) = offerDao.getOfferById(id)

    suspend fun getCategories(parentId: Int? = null) = categoryDao.getSubcategories(parentId)

    suspend fun postOffer(offer: JsonObject, images: List<Uri>): ResponseState<NewOfferResponse> {
        val body = offer.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val imageFiles = images.map {
            coroutineScope {
                async {
                    imageProvider.getCompressedFileFromGlideCache(it)
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
}