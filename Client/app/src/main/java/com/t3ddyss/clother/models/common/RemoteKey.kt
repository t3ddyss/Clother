package com.t3ddyss.clother.models.common

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "remote_key")
class RemoteKey(
        @PrimaryKey
        @ColumnInfo(collate = ColumnInfo.NOCASE)
        val list: String,
        @ColumnInfo(name = "after_key")
        val afterKey: Int?)