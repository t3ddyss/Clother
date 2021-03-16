package com.t3ddyss.clother.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "remote_keys")
class RemoteKey(
        @PrimaryKey
        @ColumnInfo(collate = ColumnInfo.NOCASE)
        val list: String,
        @SerializedName("after_key")
        val afterKey: Int?)