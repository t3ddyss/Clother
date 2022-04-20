package com.t3ddyss.feature_location.domain.models

import com.google.android.gms.maps.model.LatLng

data class LocationData(
    val latLng: LatLng = LatLng(52.23, 21.01),
    val locationType: LocationType = LocationType.INITIAL
) {
    companion object {
        val DEFAULT = LocationData()
    }
}
