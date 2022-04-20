package com.t3ddyss.clother.data.common.initialization

import android.content.Context
import com.google.android.gms.maps.MapView
import com.t3ddyss.clother.domain.common.initialization.MapRepository
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.rethrowIfCancellationException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : MapRepository {
    override fun initialize() {
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
            ex.rethrowIfCancellationException()
            log("MapRepositoryImpl.initialize() $ex")
        }
    }
}