package com.t3ddyss.clother.presentation.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.models.Offer
import com.t3ddyss.clother.domain.offer.OffersInteractor
import com.t3ddyss.clother.util.Event
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor
) : ViewModel() {
    private val _offer = MutableLiveData<Offer>()
    val offer: LiveData<Offer> = _offer

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
                val result = offersInteractor.deleteOffer(currentOffer.id)

                if (result is Success) {
                    deletedOffers.add(currentOffer.id)
                    _removedOffers.postValue(deletedOffers)
                }

                _deletionResponse.postValue(Event(result))
            }
        }
    }
}