package com.t3ddyss.clother.ui.gallery

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
        application: Application
) : AndroidViewModel(application) {
    val images = liveData(Dispatchers.IO) {
        emit(loadImages())
    }

    private fun loadImages(): List<Uri> {
        val imagesList = mutableListOf<Uri>()

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
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

        return imagesList
    }
}