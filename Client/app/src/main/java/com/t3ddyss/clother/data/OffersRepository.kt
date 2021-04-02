package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
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
import com.t3ddyss.clother.models.Category
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.CLOTHER_PAGE_SIZE
import com.t3ddyss.clother.utilities.DEBUG_TAG
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject


@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OffersRepository
@Inject constructor(
        private val service: ClotherOffersService,
        private val imagesRepository: ImagesRepository,
        private val prefs: SharedPreferences,
        private val db: AppDatabase,
        private val offerDao: OfferDao,
        private val remoteKeyDao: RemoteKeyDao,
        private val categoryDao: CategoryDao
) {

    fun getOffersStream(query: Map<String, String>): Flow<PagingData<Offer>> {
        val pagingSourceFactory = { Log.d(DEBUG_TAG, "Retrieving offers from db")
            offerDao.getAllOffers() }
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

    suspend fun getCategories(parentId: Int? = null): List<Category> {
        if (categoryDao.getCategoriesCount() == 0) {
            val categories = service.getCategories(prefs.getString(ACCESS_TOKEN, null))
            categoryDao.insertAll(categories)
        }

        return categoryDao.getSubcategories(parentId)
    }

    suspend fun postOffer(offer: JsonObject, images: List<Uri>) {
        val body = offer.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val absolutePath = imagesRepository.getAbsolutePath(images[0])
        val requestFile = File(absolutePath!!)
                .asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val image = MultipartBody.Part
                .createFormData("file", "myfile123", requestFile)
        service.postOffer(prefs.getString(ACCESS_TOKEN, null), body, image)
    }
}