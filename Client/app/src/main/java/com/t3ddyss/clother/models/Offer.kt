package com.t3ddyss.clother.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offers")
data class Offer(val title: String,
                 val address: String,
                 val image: String) {
    @PrimaryKey var id = 0 // To exclude this property from equals() method
}
