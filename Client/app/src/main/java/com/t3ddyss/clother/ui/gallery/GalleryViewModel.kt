package com.t3ddyss.clother.ui.gallery

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
        application: Application
) : AndroidViewModel(application), LifecycleObserver {

    private val _images = MutableLiveData<List<Uri>>()
    val images: LiveData<List<Uri>> get() {
        loadImages()
        return _images
    }

    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    private val imagesObserver = object : ContentObserver(
//            Handler(HandlerThread("ImagesObserverThread").apply { start() }.looper)) {
            Handler(getApplication<Application>().applicationContext.mainLooper)) {
        override fun onChange(selfChange: Boolean) {
            Log.d(DEBUG_TAG, "Content changed")
            loadImages()
        }
    }

    init {
        getApplication<Application>().contentResolver.registerContentObserver(
                uri,
                true,
                imagesObserver)
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

            _images.postValue(imagesList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(imagesObserver)
    }
}