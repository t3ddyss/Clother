package com.t3ddyss.feature_location.domain

import com.google.android.gms.maps.model.LatLng

data class LocationData(
    val latLng: LatLng = LatLng(52.23, 21.01),
    val locationType: LocationType = LocationType.INITIAL
) {
    companion object {
        val DEFAULT = LocationData()
    }
}

enum class LocationType {
    INITIAL,
    MANUALLY_SELECTED,
    DETECTED_BY_GPS
}
