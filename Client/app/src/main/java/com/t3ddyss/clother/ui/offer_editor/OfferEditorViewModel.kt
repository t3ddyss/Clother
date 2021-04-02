package com.t3ddyss.clother.ui.offer_editor

import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.Loading
import com.t3ddyss.clother.models.NewOfferResponse
import com.t3ddyss.clother.models.ResponseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OfferEditorViewModel
@Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _newOfferResponse = MutableLiveData<ResponseState<NewOfferResponse>>()
    val newNewOfferResponse: LiveData<ResponseState<NewOfferResponse>> = _newOfferResponse
    val images = MutableLiveData<MutableList<Uri>>(mutableListOf())
    val location = MutableLiveData<LatLng>()

    fun postOffer(offer: JsonObject, images: List<Uri>) {
        _newOfferResponse.value = Loading()
        viewModelScope.launch {
            _newOfferResponse.postValue(repository.postOffer(offer, images))
        }
    }
}