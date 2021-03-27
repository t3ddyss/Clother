package com.t3ddyss.clother.ui.gallery

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.t3ddyss.clother.adapters.GalleryImagesAdapter
import com.t3ddyss.clother.models.GalleryImage
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
        application: Application
) : AndroidViewModel(application) {

    private val _images = MutableLiveData<List<GalleryImage>>(listOf())
    val images: LiveData<List<GalleryImage>> = _images

//    private val _selectedImages = MutableLiveData<MutableList<GalleryImage?>>(null)
//    val selectedImages: MutableLiveData<MutableList<GalleryImage?>> = MutableLiveData(null)

    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    private val imagesObserver = object : ContentObserver(
            Handler(getApplication<Application>().applicationContext.mainLooper)) {
        override fun onChange(selfChange: Boolean) {
            if (selfChange) return
            loadImages()
        }

//        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
//            Log.d(DEBUG_TAG, "$uri item changed!")
//            when (flags) {
//                ContentResolver.NOTIFY_DELETE -> Log.d(DEBUG_TAG, "Delete")
//                ContentResolver.NOTIFY_INSERT -> Log.d(DEBUG_TAG, "Insert")
//                ContentResolver.NOTIFY_UPDATE -> Log.d(DEBUG_TAG, "Update")
//                else -> Log.d(DEBUG_TAG, "Unknown $flags")
//            }
//        }
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