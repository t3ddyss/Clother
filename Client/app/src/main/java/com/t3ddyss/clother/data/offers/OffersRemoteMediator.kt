package com.t3ddyss.clother.data.offers

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.t3ddyss.clother.data.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.Storage
import com.t3ddyss.clother.data.common.db.AppDatabase
import com.t3ddyss.clother.data.offers.db.OfferDao
import com.t3ddyss.clother.data.offers.db.RemoteKeyDao
import com.t3ddyss.clother.data.offers.db.models.OfferEntity
import com.t3ddyss.clother.data.offers.db.models.RemoteKeyEntity
import com.t3ddyss.clother.data.offers.remote.RemoteOffersService
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.rethrowIfCancellationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class OffersRemoteMediator @AssistedInject constructor(
    private val service: RemoteOffersService,
    private val storage: Storage,
    private val db: AppDatabase,
    private val offerDao: OfferDao,
    private val remoteKeyDao: RemoteKeyDao,
    @Assisted private val listKey: String,
    @Assisted private val query: Map<String, String>
) : RemoteMediator<Int, OfferEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, OfferEntity>
    ): MediatorResult {
        val key: Int? = when (loadType) {
            LoadType.REFRESH -> {
                null
            }

            LoadType.PREPEND -> {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val afterKey = db.withTransaction {
                    remoteKeyDao.remoteKeyByList(listKey).afterKey
                }

                afterKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val items = service.getOffers(
                accessToken = storage.accessToken,
                afterKey = key,
                beforeKey = null,
                limit = when (loadType) {
                    LoadType.REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                },
                filters = query
            )
                .map { it.toEntity() }
            items.forEach { it.listKey = listKey }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    offerDao.deleteAllOffersFromList(listKey)
                    remoteKeyDao.removeByList(listKey)
                }

                offerDao.insertAll(items)
                remoteKeyDao.insert(RemoteKeyEntity(listKey, items.lastOrNull()?.id))
            }

            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("OffersRemoteMediator.load() $ex")
            MediatorResult.Error(ex)
        }
    }
}

@AssistedFactory
interface OffersRemoteMediatorFactory {
    fun create(listKey: String, query: Map<String, String>): OffersRemoteMediator
}