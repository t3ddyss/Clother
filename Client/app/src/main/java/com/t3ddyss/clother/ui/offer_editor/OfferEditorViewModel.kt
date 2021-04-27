package com.t3ddyss.clother.ui.offer_editor

import android.net.Uri
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.domain.Loading
import com.t3ddyss.clother.models.domain.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferEditorViewModel
@Inject constructor(
    private val repository: OffersRepository
) : ViewModel() {
    private val _newOfferResponse = MutableLiveData<Resource<*>>()
    val newNewOfferResponse: LiveData<Resource<*>> = _newOfferResponse
    val images = MutableLiveData<MutableList<Uri>>(mutableListOf())
    val location = MutableLiveData<LatLng>()

    fun postOffer(offer: JsonObject, images: List<Uri>) {
        _newOfferResponse.value = Loading(null)
        viewModelScope.launch {
            _newOfferResponse.postValue(repository.postOffer(offer, images))
        }
    }
}