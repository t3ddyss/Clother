package com.t3ddyss.clother.ui.gallery

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.models.GalleryImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
        application: Application,
        private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _images = MutableLiveData<List<GalleryImage>>()
    val images: LiveData<List<GalleryImage>> = _images

    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    private val imagesObserver = object : ContentObserver(
            Handler(getApplication<Application>().applicationContext.mainLooper)) {
        override fun onChange(selfChange: Boolean) {
            if (selfChange) return
            loadImages()
        }
    }

    init {
        getApplication<Application>().contentResolver.registerContentObserver(
                uri,
                true,
                imagesObserver)
    }

    fun getImages() {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val imagesList = mutableListOf<Uri>()

            val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_ADDED)

            val cursor = getApplication<Application>().contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    "DATE_ADDED DESC"
            )

            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                while (it.moveToNext()) {
                    val imageId = it.getLong(columnIndex)
                    val imageUri = Uri.withAppendedPath(uri, imageId.toString())
                    imagesList.add(imageUri)
                }
            }

            _images.postValue(imagesList.map { GalleryImage(it) })
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(imagesObserver)
    }
}