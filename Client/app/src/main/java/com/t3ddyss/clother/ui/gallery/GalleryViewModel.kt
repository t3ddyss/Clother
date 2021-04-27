package com.t3ddyss.clother.ui.gallery

import androidx.lifecycle.*
import com.t3ddyss.clother.data.ImageProvider
import com.t3ddyss.clother.models.domain.MediaImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
        private val repository: ImageProvider,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _images = MutableLiveData<List<MediaImage>>()
    val images: LiveData<List<MediaImage>> = _images
    private var isInitialImagesLoaded = AtomicBoolean(false)

    fun getImages() {
        if (isInitialImagesLoaded.getAndSet(true)) return

        viewModelScope.launch {
            repository.getImagesStream()
                    .map { list -> list.map { MediaImage(it) } }
                    .collectLatest {
                        _images.postValue(it)
            }
        }
    }
}