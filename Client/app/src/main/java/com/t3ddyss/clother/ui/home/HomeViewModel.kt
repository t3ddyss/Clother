package com.t3ddyss.clother.ui.home

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: OffersRepository
) : ViewModel() {

    @ExperimentalPagingApi
    val offers = liveData {
        repository
            .getOffers()
            .cachedIn(viewModelScope)
            .collectLatest {
                emit(it)
            }
    }

    private val _newOfferAdded = MutableLiveData<Event<Int>>()
    val newOfferAdded: LiveData<Event<Int>> = _newOfferAdded

    var endOfPaginationReachedBottom = false

    fun setNewOfferAdded(offerId: Int) {
        if (newOfferAdded.value?.peekContent() == offerId) return
        _newOfferAdded.value = Event(offerId)
    }
}