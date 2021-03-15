package com.t3ddyss.clother.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.models.Offer

@ExperimentalPagingApi
class OffersRemoteMediator(
    private val query: Map<String, String>,
    private val service: ClotherOffersService,
    private val appDatabase: AppDatabase
) : RemoteMediator<Int, Offer>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Offer>): MediatorResult {
        TODO("Not yet implemented")
    }
}