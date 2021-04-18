package com.t3ddyss.clother.ui.home

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.offers.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class HomeViewModel
@Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    private val isOffersLoaded = AtomicBoolean(false)
    var endOfPaginationReachedBottom = false

    fun getOffers() {
        if (isOffersLoaded.getAndSet(true)) return

        viewModelScope.launch {
            repository
                    .getOffers()
                    .cachedIn(viewModelScope)
                    .collectLatest {
                        _offers.postValue(it)
                    }
        }
    }
}