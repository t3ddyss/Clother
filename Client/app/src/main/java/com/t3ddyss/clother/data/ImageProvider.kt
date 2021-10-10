package com.t3ddyss.clother.data

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import com.bumptech.glide.Glide
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ImageProvider @Inject constructor(
    private val application: Application
) {
    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    suspend fun observeImages() = callbackFlow {
        trySend(loadGalleryImages())

        val imageUpdatesObserver =
            object : ContentObserver(Handler(application.applicationContext.mainLooper)) {
                override fun onChange(selfChange: Boolean) {
                    if (selfChange) return
                    trySend(loadGalleryImages())
                }
            }
        application.contentResolver.registerContentObserver(
            uri,
            true,
            imageUpdatesObserver
        )
        awaitClose {
            application.contentResolver.unregisterContentObserver(imageUpdatesObserver)
        }
    }.flowOn(Dispatchers.IO)

    private fun loadGalleryImages(): List<Uri> {
        val images = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
        )
        val query = (MediaStore.Images.Media.MIME_TYPE + "='image/jpeg'" + " OR "
                + MediaStore.Images.Media.MIME_TYPE + "='image/png'" + " OR "
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
        Dispatchers.IO
    )
}