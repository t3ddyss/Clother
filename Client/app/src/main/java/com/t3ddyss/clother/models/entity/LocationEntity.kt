package com.t3ddyss.clother.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationEntity(@PrimaryKey(autoGenerate = true)
                          val id: Int = 0,
                          val lat: Double,
                          val lng: Double
)