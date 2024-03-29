package com.t3ddyss.clother.data.offers.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "category",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("parent_id")
    )]
)
data class CategoryEntity(
    @PrimaryKey val id: Int,

    @ColumnInfo(name = "parent_id", index = true)
    val parentId: Int?,

    val title: String
)