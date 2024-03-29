package com.t3ddyss.clother.presentation.offers.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.offers.ImagesInteractor
import com.t3ddyss.clother.domain.offers.models.MediaImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val imagesInteractor: ImagesInteractor
) : ViewModel() {
    private val _images = MutableLiveData<List<MediaImage>>()
    val images: LiveData<List<MediaImage>> = _images

    init {
        viewModelScope.launch {
            imagesInteractor.observeLocalImages()
                .map { list -> list.map { MediaImage(it) } }
                .collectLatest {
                    _images.postValue(it)
                }
        }
    }
}