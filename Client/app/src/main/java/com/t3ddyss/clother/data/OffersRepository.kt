package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
class OffersRepository @Inject constructor(
    private val service: ClotherOffersService,
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
}