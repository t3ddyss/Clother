package com.t3ddyss.clother.data.common.db

import androidx.room.TypeConverter
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import java.util.*

class Converters {
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

    @TypeConverter
    fun toMessageStatus(value: Int) = enumValues<MessageStatus>()[value]

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus) = value.ordinal
}