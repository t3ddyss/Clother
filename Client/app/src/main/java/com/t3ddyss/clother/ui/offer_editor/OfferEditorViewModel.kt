package com.t3ddyss.clother.ui.offer_editor

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.t3ddyss.clother.data.ImagesRepository
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.GalleryImage
import com.t3ddyss.clother.models.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class OfferEditorViewModel
@Inject constructor(
        private val repository: OffersRepository,
        private val imagesRepository: ImagesRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val images = MutableLiveData<MutableList<Uri>>(mutableListOf())
    val location = MutableLiveData<LatLng>()

    fun postOffer(offer: JsonObject, images: List<Uri>) {
        viewModelScope.launch {
            repository.postOffer(offer, images)
        }
    }
}