package com.t3ddyss.clother.data

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.t3ddyss.clother.domain.offer.ImagesRepository
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    private val application: Application
) : ImagesRepository {
    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override suspend fun observeImages() = callbackFlow {
        trySend(queryLocalImages())

        val imagesUpdatesObserver =
            object : ContentObserver(Handler(application.applicationContext.mainLooper)) {
                override fun onChange(selfChange: Boolean) {
                    if (selfChange) return
                    trySend(queryLocalImages())
                }
            }
        application.contentResolver.registerContentObserver(
            uri,
            true,
            imagesUpdatesObserver
        )
        awaitClose {
            application.contentResolver.unregisterContentObserver(imagesUpdatesObserver)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getCompressedImage(uri: Uri) = withContext(Dispatchers.IO) {
        val imageFile = Glide.with(application).asFile().load(uri).submit().get()
        Compressor.compress(
            application.applicationContext,
            imageFile,
            Dispatchers.IO
        )
    }

    private fun queryLocalImages(): List<Uri> {
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
}