package com.t3ddyss.clother.presentation.home

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.t3ddyss.clother.domain.offer.OffersInteractor
import com.t3ddyss.clother.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor
) : ViewModel() {

    val offers = liveData {
        offersInteractor
            .observeOffersFromDatabase()
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