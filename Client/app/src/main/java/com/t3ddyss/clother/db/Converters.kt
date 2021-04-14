package com.t3ddyss.clother.db

import androidx.room.TypeConverter
import java.util.*

class Converters {
    // Violates 1NF, but we don't need to update an offer in our database
    @TypeConverter
    fun fromListOfStrings(images: List<String>) = images.joinToString(separator = ";")

    @TypeConverter
    fun toListOfStrings(images: String) = images.split(";")

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}