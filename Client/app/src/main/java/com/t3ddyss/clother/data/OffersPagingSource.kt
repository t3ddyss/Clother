package com.t3ddyss.clother.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.utilities.CLOTHER_STARTING_PAGE_INDEX
import java.lang.Exception

class OffersPagingSource(private val service: ClotherOffersService,
                         private val query: Map<String, String>
): PagingSource<Int, Offer>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> {
        val page = params.key ?: CLOTHER_STARTING_PAGE_INDEX

        return try {
            val response = service.getOffers(page, params.loadSize, query)
            val results = response.results
            LoadResult.Page(
                data = results,
                prevKey = if (page == CLOTHER_STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if (page == response.totalPages) null else page + 1
            )

        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Offer>): Int? {
        return state.anchorPosition
    }
}