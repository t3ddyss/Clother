package com.t3ddyss.clother.presentation.offer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.t3ddyss.clother.domain.offer.OffersInteractor
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferEditorViewModel
@Inject constructor(
    private val offersInteractor: OffersInteractor
) : ViewModel() {

    private val _newOfferResponse = MutableLiveData<Resource<*>>()
    val newNewOfferResponse: LiveData<Resource<*>> = _newOfferResponse
    private val _location = MutableLiveData<LatLng>()
    val location: LiveData<LatLng> = _location
    val images = MutableLiveData<MutableList<Uri>>(mutableListOf())

    fun selectLocation(location: String) {
        val (lat, lng) = location.split(",").map { it.toDouble() }
        _location.value = LatLng(lat, lng)
    }

    fun postOffer(offer: JsonObject, images: List<Uri>) {
        _newOfferResponse.value = Loading(null)
        viewModelScope.launch {
            _newOfferResponse.postValue(offersInteractor.postOffer(offer, images))
        }
    }
}