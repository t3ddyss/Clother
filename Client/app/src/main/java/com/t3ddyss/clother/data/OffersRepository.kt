package com.t3ddyss.clother.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.utilities.CLOTHER_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OffersRepository @Inject constructor(private val service: ClotherOffersService)
{
    fun getOffersStream(query: Map<String, String>): Flow<PagingData<Offer>> {
        return Pager(
                config = PagingConfig(pageSize = CLOTHER_PAGE_SIZE, enablePlaceholders = false,
                initialLoadSize = CLOTHER_PAGE_SIZE),
                pagingSourceFactory = { OffersPagingSource(service, query) }
        ).flow
    }
}