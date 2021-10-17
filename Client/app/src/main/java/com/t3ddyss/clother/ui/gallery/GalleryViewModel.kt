package com.t3ddyss.clother.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.ImageProvider
import com.t3ddyss.clother.models.domain.MediaImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: ImageProvider
) : ViewModel() {
    private val _images = MutableLiveData<List<MediaImage>>()
    val images: LiveData<List<MediaImage>> = _images

    init {
        viewModelScope.launch {
            repository.observeImages()
                .map { list -> list.map { MediaImage(it) } }
                .collectLatest {
                    _images.postValue(it)
                }
        }
    }
}