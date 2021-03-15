package com.t3ddyss.clother.data

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams.Append
import androidx.paging.PagingSource.LoadParams.Prepend
import androidx.paging.PagingState
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.models.Offer

class OffersPagingSource(private val service: ClotherOffersService,
                         private val query: Map<String, String>
): PagingSource<Int, Offer>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> {
        return try {
            val items = service.getOffers(
                        afterKey = if (params is Append) params.key else null,
                        beforeKey = if (params is Prepend) params.key else null,
                        size = params.loadSize,
                        filters = query)

            LoadResult.Page(
                data = items,
                prevKey = items.firstOrNull()?.id,
                nextKey = items.lastOrNull()?.id
            )

        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Offer>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.id
        }
    }
}