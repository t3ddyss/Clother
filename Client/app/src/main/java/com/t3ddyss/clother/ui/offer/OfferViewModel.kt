package com.t3ddyss.clother.ui.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.offers.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class OfferViewModel @Inject constructor(
        private val repository: OffersRepository
) : ViewModel() {
    private val _offer = MutableLiveData<Offer>()
    val offer: LiveData<Offer> = _offer

    fun selectOffer(offer: Offer) {
        _offer.value = offer
    }
}