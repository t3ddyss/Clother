package com.t3ddyss.clother.ui.search_results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class SearchResultsViewModel @Inject constructor(
    private val repository: OffersRepository
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    private var currentQuery: Map<String, String>? = null
    var endOfPaginationReachedBottom = false

    fun getOffers(query: Map<String, String>) {
        if (query == currentQuery) {
            return
        }
        currentQuery = query

        viewModelScope.launch {
            repository
                    .getOffers(query)
                    .cachedIn(viewModelScope)
                    .collectLatest {
                        _offers.postValue(it)
                    }
        }
    }
}