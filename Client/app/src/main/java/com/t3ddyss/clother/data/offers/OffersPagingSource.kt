package com.t3ddyss.clother.data.offers

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams.Append
import androidx.paging.PagingSource.LoadParams.Prepend
import androidx.paging.PagingState
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.offers.remote.RemoteOffersService
import com.t3ddyss.clother.data.offers.remote.models.OfferDto
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.rethrowIfCancellationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class OffersPagingSource @AssistedInject constructor(
    private val service: RemoteOffersService,
    private val storage: Storage,
    @Assisted private val query: Map<String, String>
) : PagingSource<Int, OfferDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OfferDto> {
        return try {
            val items = service.getOffers(
                accessToken = storage.accessToken,
                afterKey = if (params is Append) params.key else null,
                beforeKey = if (params is Prepend) params.key else null,
                limit = params.loadSize,
                filters = query
            )

            LoadResult.Page(
                data = items,
                prevKey = items.firstOrNull()?.id,
                nextKey = items.lastOrNull()?.id
            )

        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("OffersPagingSource.load(params=$params) $ex")
            LoadResult.Error(ex)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, OfferDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.id
        }
    }
}

@AssistedFactory
interface OffersPagingSourceFactory {
    fun create(query: Map<String, String>): OffersPagingSource
}