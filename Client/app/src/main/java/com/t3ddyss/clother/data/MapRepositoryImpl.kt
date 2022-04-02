package com.t3ddyss.clother.data

import android.content.Context
import com.google.android.gms.maps.MapView
import com.t3ddyss.clother.domain.MapRepository
import com.t3ddyss.core.util.log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : MapRepository {
    override suspend fun initialize() {
        try {
            MapView(context).apply {
                onCreate(null)
                onStart()
                onResume()
                onPause()
                onStop()
                onDestroy()
            }
        } catch (ex: Exception) {
            if (ex is CancellationException) throw ex
            log("Failed to initialize Google Map")
        }
    }
}