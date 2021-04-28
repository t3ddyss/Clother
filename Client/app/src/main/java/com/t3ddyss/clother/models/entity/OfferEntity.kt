package com.t3ddyss.clother.models.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.*

@Entity(
    tableName = "offer",
    primaryKeys = ["id", "list_key"],
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("category_id")
    )]
)
data class OfferEntity(
    var id: Int = 0,

    @ColumnInfo(name = "list_key")
    var listKey: String = "offers",

    @ColumnInfo(name = "user_id")
    var userId: Int = 0,

    @ColumnInfo(name = "category_id", index = true)
    var categoryId: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Date,

    var title: String = "",
    var description: String? = null,

    @ColumnInfo(name = "user_name")
    var userName: String = "",

    var category: String = "",
    // Violates 1NF, but we don't need to update an offer in our database for now
    var images: List<String> = listOf(),
    val size: String? = null,
    var location: String? = null
)
