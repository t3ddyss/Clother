package com.t3ddyss.clother.ui.offer_editor

import android.net.Uri
import android.widget.Gallery
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.GalleryImage
import com.t3ddyss.clother.utilities.SELECTED_IMAGES
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class OfferEditorViewModel
@Inject constructor(
        private val repository: OffersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
}