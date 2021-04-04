package com.t3ddyss.clother.db

import androidx.room.TypeConverter

class Converters {
    // Violates 1NF, but we don't need to update an offer in our database
    @TypeConverter
    fun fromListOfImages(images: List<String>) = images.joinToString(separator = ",")

    @TypeConverter
    fun toListOfImages(images: String) = images.split(",")
}