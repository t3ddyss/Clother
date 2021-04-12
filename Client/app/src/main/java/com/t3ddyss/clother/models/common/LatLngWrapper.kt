package com.t3ddyss.clother.models.common

import com.google.android.gms.maps.model.LatLng

data class LatLngWrapper(val latLng: LatLng = LatLng(52.23, 21.01),
                         val isInitialValue: Boolean = true,
                         val isManuallySelected: Boolean = false
)
