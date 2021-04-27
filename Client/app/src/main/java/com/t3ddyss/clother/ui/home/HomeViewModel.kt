package com.t3ddyss.clother.ui.home

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.domain.Offer
import com.t3ddyss.clother.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: OffersRepository
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers
    private val _newOfferAdded = MutableLiveData<Event<Int>>()
    val newOfferAdded: LiveData<Event<Int>> = _newOfferAdded

    private val isOffersLoaded = AtomicBoolean(false)
    var endOfPaginationReachedBottom = false

    @ExperimentalPagingApi
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

    fun setNewOfferAdded(offerId: Int) {
        if (newOfferAdded.value?.peekContent() == offerId) return
        _newOfferAdded.value = Event(offerId)
    }
}