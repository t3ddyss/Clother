package com.t3ddyss.clother.data

import android.app.Application
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import com.t3ddyss.clother.utilities.DEBUG_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ImagesRepository @Inject constructor(
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
    }

    suspend fun getAbsolutePath(uri: Uri) = withContext(Dispatchers.IO) {
        val filePath = (application.applicationInfo.dataDir + File.separator
                + System.currentTimeMillis())
        val file = File(filePath)

        // These methods don't block anything because they are wrapped by withContext(Dispatchers.IO)
        try {
            val inputStream= application.contentResolver.openInputStream(uri)
            inputStream ?: return@withContext null

            val outputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int

            while (inputStream.read(buf).also { len = it } > 0) {
                outputStream.write(buf, 0, len)
            }

            outputStream.close()
            inputStream.close()
        } catch (ex: IOException) {
            return@withContext null
        }

        return@withContext file.absolutePath
    }

    private fun loadImagesFromGallery(): List<Uri> {
        val images = mutableListOf<Uri>()

        val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = application.contentResolver.query(
                uri,
                projection,
                null,
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