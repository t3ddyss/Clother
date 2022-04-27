package com.t3ddyss.clother.presentation.chat

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.offers.ImagesInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageSelectorViewModel @Inject constructor(
    private val imagesInteractor: ImagesInteractor
) : ViewModel() {
    private val _images = MutableLiveData<List<Uri>>()
    val images: LiveData<List<Uri>> = _images

    init {
        viewModelScope.launch {
            imagesInteractor
                .observeLocalImages()
                .collectLatest {
                    _images.postValue(it)
                }
        }
    }
}