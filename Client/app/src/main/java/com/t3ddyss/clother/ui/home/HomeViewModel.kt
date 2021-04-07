package com.t3ddyss.clother.ui.home

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class HomeViewModel
@Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    private var currentQuery: Map<String, String>? = null
    var endOfPaginationReachedBottom = false

    fun getOffers(query: Map<String, String> = mapOf()) {
        if (query == currentQuery) {
            return
        }
        currentQuery = query

        viewModelScope.launch {
            repository
                    .getOffers(query, REMOTE_KEY_HOME)
                    .cachedIn(viewModelScope)
                    .collectLatest {
                        _offers.postValue(it)
                    }
        }
    }

    companion object {
        const val REMOTE_KEY_HOME = "home"
    }
}