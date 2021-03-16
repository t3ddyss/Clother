package com.t3ddyss.clother.data

import android.util.Log
import androidx.paging.*
import androidx.room.withTransaction
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.OfferDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.models.RemoteKey
import com.t3ddyss.clother.utilities.DEBUG_TAG
import java.lang.Exception

@ExperimentalPagingApi
class OffersRemoteMediator(
    private val query: Map<String, String>,
    private val service: ClotherOffersService,
    private val db: AppDatabase,
    private val offerDao: OfferDao,
    private val remoteKeyDao: RemoteKeyDao
) : RemoteMediator<Int, Offer>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Offer>): MediatorResult {
        val key: Int? = when (loadType) {
            LoadType.REFRESH -> {
                Log.d(DEBUG_TAG, "REFRESH")

                null
            }

            LoadType.PREPEND -> {
                Log.d(DEBUG_TAG, "PREPEND")

                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val afterKey = db.withTransaction {
                    remoteKeyDao.remoteKeyByList("offers_home").afterKey
                }

                Log.d(DEBUG_TAG, "APPEND ${afterKey ?: "NULL"}")

                afterKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val items = service.getOffers(
                    afterKey = key,
                    beforeKey = null,
                    size = when(loadType) {
                        LoadType.REFRESH -> state.config.initialLoadSize
                        else -> state.config.pageSize
                    },
                    filters = query)

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    offerDao.deleteAllOffers()
                    remoteKeyDao.removeByList("offers_home")
                }

                offerDao.insertAll(items)
                remoteKeyDao.insert(RemoteKey("offers_home", items.lastOrNull()?.id))
            }

            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        }

        catch (ex: Exception) {
            Log.d(DEBUG_TAG, ex.toString())
            MediatorResult.Error(ex)
        }
    }
}