package com.t3ddyss.clother.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.clother.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    offersInteractor: OffersInteractor
) : ViewModel() {

    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    init {
        offersInteractor
            .observeOffersFromDatabase()
            .cachedIn(viewModelScope)
            .onEach { _offers.value = it }
            .launchIn(viewModelScope)
    }

    private val _newOfferAdded = MutableLiveData<Event<Int>>()
    val newOfferAdded: LiveData<Event<Int>> = _newOfferAdded

    var endOfPaginationReachedBottom = false

    fun setNewOfferAdded(offerId: Int) {
        if (newOfferAdded.value?.peekContent() == offerId) return
        _newOfferAdded.value = Event(offerId)
    }
}