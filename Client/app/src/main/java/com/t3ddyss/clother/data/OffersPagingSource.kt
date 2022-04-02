package com.t3ddyss.clother.data

import android.content.SharedPreferences
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams.Append
import androidx.paging.PagingSource.LoadParams.Prepend
import androidx.paging.PagingState
import com.t3ddyss.clother.data.Mappers.toDomain
import com.t3ddyss.clother.data.remote.RemoteOffersService
import com.t3ddyss.clother.domain.models.Offer
import com.t3ddyss.clother.util.ACCESS_TOKEN

class OffersPagingSource(
    private val service: RemoteOffersService,
    private val prefs: SharedPreferences,
    private val query: Map<String, String>
) : PagingSource<Int, Offer>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> {
        return try {
            val items = service.getOffers(
                accessToken = prefs.getString(ACCESS_TOKEN, null),
                afterKey = if (params is Append) params.key else null,
                beforeKey = if (params is Prepend) params.key else null,
                limit = params.loadSize,
                filters = query
            )
                .map { it.toDomain() }

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