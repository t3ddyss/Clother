package com.t3ddyss.clother.models.domain

import com.google.android.gms.maps.model.LatLng

data class LocationData(
    val latLng: LatLng = LatLng(52.23, 21.01),
    val isInitialValue: Boolean = true,
    val isManuallySelected: Boolean = false
)
