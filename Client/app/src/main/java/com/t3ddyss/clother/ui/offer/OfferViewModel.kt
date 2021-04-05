package com.t3ddyss.clother.ui.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OfferViewModel @Inject constructor(
        private val repository: OffersRepository
) : ViewModel() {
    private val _offer = MutableLiveData<Offer>()
    val offer: LiveData<Offer> = _offer

    fun getOffer(id: Int) {
        viewModelScope.launch {
            _offer.postValue(repository.getOfferById(id))
        }
    }
}