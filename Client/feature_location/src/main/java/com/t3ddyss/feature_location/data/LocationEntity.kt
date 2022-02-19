package com.t3ddyss.feature_location.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val lat: Double,
    val lng: Double
)
