package com.t3ddyss.clother.ui.offer_editor

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class OfferEditorViewModel
@Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _images = MutableLiveData<MutableList<Uri?>>(mutableListOf(null))
    val images: LiveData<MutableList<Uri?>> = _images
}