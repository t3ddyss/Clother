package com.t3ddyss.clother.data.offers.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_key")
class RemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val list: String,

    @ColumnInfo(name = "after_key")
    val afterKey: Int?
)