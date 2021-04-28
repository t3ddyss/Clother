package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.OfferDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.entity.OfferEntity
import com.t3ddyss.clother.models.entity.RemoteKeyEntity
import com.t3ddyss.clother.models.mappers.mapOfferDtoToEntity
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.DEBUG_TAG

@ExperimentalPagingApi
class OffersRemoteMediator(
    private val service: ClotherOffersService,
    prefs: SharedPreferences,
    private val db: AppDatabase,
    private val offerDao: OfferDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val listKey: String,
    private val query: Map<String, String>
) : RemoteMediator<Int, OfferEntity>() {
    private var accessToken: String? = null
    private var changeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            run {
                if (key == ACCESS_TOKEN) {
                    accessToken = sp.getString(key, null)
                }
            }
        }

    init {
        accessToken = prefs.getString(ACCESS_TOKEN, null)
        prefs.registerOnSharedPreferenceChangeListener(changeListener)
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, OfferEntity>
    ): MediatorResult {
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
                    remoteKeyDao.remoteKeyByList(listKey).afterKey
                }

                Log.d(DEBUG_TAG, "APPEND ${afterKey ?: "NULL"}")

                afterKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val items = service.getOffers(
                accessToken = accessToken,
                afterKey = key,
                beforeKey = null,
                limit = when (loadType) {
                    LoadType.REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                },
                filters = query
            )
                .map { mapOfferDtoToEntity(it) }
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
            Log.d(DEBUG_TAG, "Mediator $ex")
            MediatorResult.Error(ex)
        }
    }
}