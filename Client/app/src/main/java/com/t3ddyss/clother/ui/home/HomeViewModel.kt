package com.t3ddyss.clother.ui.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class HomeViewModel
@Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentQuery: Map<String, String>? = null
    private var currentResult: Flow<PagingData<Offer>>? = null

    fun getOffers(query: Map<String, String>): Flow<PagingData<Offer>> {
        val lastResult = currentResult
        if (query == currentQuery && lastResult != null) {
            return lastResult
        }

        currentQuery = query
        val newResult = repository
                .getOffersStream(query)
                .cachedIn(viewModelScope)
        currentResult = newResult
        return newResult
    }
}