package com.t3ddyss.clother.db

import androidx.room.TypeConverter
import com.t3ddyss.clother.models.domain.MessageStatus
import java.util.*

class Converters {
    // Violates 1NF, but we don't need to update an offer in our database yet
    @TypeConverter
    fun fromListOfStrings(images: List<String>) = images.joinToString(separator = ";")

    @TypeConverter
    fun toListOfStrings(images: String) = images.split(";")

    // TODO add timezone support
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toMessageStatus(value: Int) = enumValues<MessageStatus>()[value]

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus) = value.ordinal
}