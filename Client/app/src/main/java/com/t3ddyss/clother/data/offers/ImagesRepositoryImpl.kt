package com.t3ddyss.clother.data.offers

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.t3ddyss.clother.domain.offers.ImagesRepository
import com.t3ddyss.clother.util.DispatchersProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ImagesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatchersProvider
) : ImagesRepository {
    private val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override suspend fun observeImages() = callbackFlow {
        trySend(queryLocalImages())

        val imagesUpdatesObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                if (selfChange) return
                trySend(queryLocalImages())
            }
        }
        context.contentResolver.registerContentObserver(
            uri,
            true,
            imagesUpdatesObserver
        )

        awaitClose {
            context.contentResolver.unregisterContentObserver(imagesUpdatesObserver)
        }
    }.flowOn(dispatchers.io)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getCompressedImage(image: Uri): File = withContext(dispatchers.io) {
        val imageFile = Glide.with(context).asFile().load(image).submit().get()
        Compressor.compress(
            context,
            imageFile
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

        val cursor = context.contentResolver.query(
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