package com.t3ddyss.clother.data

import android.content.SharedPreferences
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams.Append
import androidx.paging.PagingSource.LoadParams.Prepend
import androidx.paging.PagingState
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.models.domain.Offer
import com.t3ddyss.clother.models.mappers.mapOfferDtoToDomain
import com.t3ddyss.clother.utilities.ACCESS_TOKEN

class OffersPagingSource(
    private val service: ClotherOffersService,
    prefs: SharedPreferences,
    private val query: Map<String, String>
) : PagingSource<Int, Offer>() {
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

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Offer> {
        return try {
            val items = service.getOffers(
                accessToken = accessToken,
                afterKey = if (params is Append) params.key else null,
                beforeKey = if (params is Prepend) params.key else null,
                limit = params.loadSize,
                filters = query
            )
                .map { mapOfferDtoToDomain(it) }

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