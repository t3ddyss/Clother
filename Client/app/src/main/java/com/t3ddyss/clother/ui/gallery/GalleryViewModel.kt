package com.t3ddyss.clother.ui.gallery

import androidx.lifecycle.*
import com.t3ddyss.clother.data.ImagesRepository
import com.t3ddyss.clother.models.GalleryImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class GalleryViewModel @Inject constructor(
        private val repository: ImagesRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _images = MutableLiveData<List<GalleryImage>>()
    val images: LiveData<List<GalleryImage>> = _images
    private var isInitialImagesLoaded = false

    fun getImages() {
        if (isInitialImagesLoaded) return
        viewModelScope.launch {
            isInitialImagesLoaded = true
            val initialImages = repository.getInitialImages()
            val updatedImages = repository.getImageUpdates()

            merge(initialImages, updatedImages)
                    .map { list -> list.map { GalleryImage(it) } }
                    .collectLatest {
                        _images.postValue(it)
            }
        }
    }
}