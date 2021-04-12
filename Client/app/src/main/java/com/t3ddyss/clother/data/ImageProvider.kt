package com.t3ddyss.clother.data

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.Glide
import com.t3ddyss.clother.utilities.DEBUG_TAG
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ImageProvider @Inject constructor(
        private val application: Application) {
    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    suspend fun getInitialImages() = flow {
        Log.d(DEBUG_TAG, "Going to emit initial images")

        emit(loadImagesFromGallery())
    }.flowOn(Dispatchers.IO)

    suspend fun getImageUpdates() = callbackFlow<List<Uri>> {
        val newImagesObserver = object : ContentObserver(Handler(application.applicationContext.mainLooper)) {
            override fun onChange(selfChange: Boolean) {
                if (selfChange) return
                Log.d(DEBUG_TAG, "Going to offer new images")

                offer(loadImagesFromGallery())
            }
        }
        application.contentResolver.registerContentObserver(
                uri,
                true,
                newImagesObserver)
        awaitClose {
            application.contentResolver.unregisterContentObserver(newImagesObserver)
        }
    }.flowOn(Dispatchers.IO)

    private fun loadImagesFromGallery(): List<Uri> {
        val images = mutableListOf<Uri>()

        val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
        )
        val query = (MediaStore.Images.Media.MIME_TYPE + "='image/jpeg'"+ " OR "
                + MediaStore.Images.Media.MIME_TYPE + "='image/png'"+ " OR "
                + MediaStore.Images.Media.MIME_TYPE + "='image/jpg'")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = application.contentResolver.query(
                uri,
                projection,
                query,
                null,
                sortOrder
        )

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (it.moveToNext()) {
                val imageId = it.getLong(columnIndex)
                val imageUri = Uri.withAppendedPath(uri, imageId.toString())
                images.add(imageUri)
            }
        }

        return images
    }

    suspend fun getCompressedImageFile(uri: Uri) = withContext(Dispatchers.IO) {
        compressImage(Glide.with(application).asFile().load(uri).submit().get())
    }

    private suspend fun compressImage(image: File) = Compressor.compress(
        application.applicationContext,
        image,
        Dispatchers.IO)
}