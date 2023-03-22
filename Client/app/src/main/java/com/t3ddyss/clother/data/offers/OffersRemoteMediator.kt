package com.t3ddyss.clother.data.offers

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import arrow.core.merge
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.common.common.Mappers.toApiCallError
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.data.offers.db.OfferDao
import com.t3ddyss.clother.data.offers.db.RemoteKeyDao
import com.t3ddyss.clother.data.offers.db.models.OfferWithUserEntity
import com.t3ddyss.clother.data.offers.db.models.RemoteKeyEntity
import com.t3ddyss.clother.data.offers.remote.RemoteOffersService
import com.t3ddyss.core.util.log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class OffersRemoteMediator @AssistedInject constructor(
    private val service: RemoteOffersService,
    private val storage: Storage,
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val offerDao: OfferDao,
    private val remoteKeyDao: RemoteKeyDao,
    @Assisted private val listKey: String,
    @Assisted private val query: Map<String, String>
) : RemoteMediator<Int, OfferWithUserEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, OfferWithUserEntity>
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

        return service.getOffers(
            accessToken = storage.accessToken,
            afterKey = key,
            beforeKey = null,
            limit = when (loadType) {
                LoadType.REFRESH -> state.config.initialLoadSize
                else -> state.config.pageSize
            },
            filters = query
        )
            .tap { items ->
                db.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        // TODO remove unreferenced users
                        offerDao.deleteAllOffersFromList(listKey)
                        remoteKeyDao.removeByList(listKey)
                    }

                    userDao.insertAll(items.map { it.user.toEntity() })
                    offerDao.insertAll(items.map { it.toEntity(listKey) })
                    remoteKeyDao.insert(RemoteKeyEntity(listKey, items.lastOrNull()?.id))
                }
            }
            .tapLeft { log("OffersRemoteMediator.load() $it") }
            .map { MediatorResult.Success(it.isEmpty()) }
            .mapLeft { MediatorResult.Error(PagingErrorWrapperException(it.toApiCallError())) }
            .merge()
    }
}

@AssistedFactory
interface OffersRemoteMediatorFactory {
    fun create(listKey: String, query: Map<String, String>): OffersRemoteMediator
}