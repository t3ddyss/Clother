package com.t3ddyss.clother.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.OfferDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.utilities.CLOTHER_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
class OffersRepository @Inject constructor(
    private val service: ClotherOffersService,
    private val db: AppDatabase,
    private val offerDao: OfferDao,
    private val remoteKeyDao: RemoteKeyDao)
{

    fun getOffersStream(query: Map<String, String>): Flow<PagingData<Offer>> {
        val pagingSourceFactory = { db.offerDao().getAllOffers() }
        return Pager(
                config = PagingConfig(
                        pageSize = CLOTHER_PAGE_SIZE,
                        enablePlaceholders = false),
                remoteMediator = OffersRemoteMediator(
                        query = query,
                        service = service,
                        db = db,
                        offerDao = offerDao,
                        remoteKeyDao = remoteKeyDao),
                pagingSourceFactory = pagingSourceFactory
        ).flow
    }
}