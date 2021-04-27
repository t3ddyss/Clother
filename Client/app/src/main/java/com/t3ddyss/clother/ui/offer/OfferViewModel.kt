package com.t3ddyss.clother.ui.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.domain.Offer
import com.t3ddyss.clother.models.domain.Resource
import com.t3ddyss.clother.models.domain.Success
import com.t3ddyss.clother.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class OfferViewModel @Inject constructor(
        private val repository: OffersRepository
) : ViewModel() {
    private val _offer = MutableLiveData<Offer>()
    val offerEntity: LiveData<Offer> = _offer

    private val _removedOffers = MutableLiveData<Set<Int>>(setOf())
    val removedOffers: LiveData<Set<Int>> = _removedOffers

    private val _deletionResponse = MutableLiveData<Event<Resource<*>>>()
    val deletionResponse: LiveData<Event<Resource<*>>> = _deletionResponse

    private val deletedOffers = mutableSetOf<Int>()

    fun selectOffer(offer: Offer) {
        _offer.value = offer
    }

    fun deleteOffer() {
        val currentOffer = _offer.value

        if (currentOffer != null && _removedOffers.value?.contains(currentOffer.id) == false) {
            viewModelScope.launch {
                val result = repository.deleteOffer(currentOffer)

                if (result is Success) {
                    deletedOffers.add(currentOffer.id)
                    _removedOffers.postValue(deletedOffers)
                }

                _deletionResponse.postValue(Event(result))
            }
        }
    }
}