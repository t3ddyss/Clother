package com.t3ddyss.clother.data.offers.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.t3ddyss.clother.data.auth.db.models.UserEntity
import java.util.*

@Entity(
    tableName = "offer",
    primaryKeys = ["offer_id", "list_key"],
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("category_id")
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [Index(value = ["user_id"])]
)
data class OfferEntity(
    @ColumnInfo(name = "offer_id")
    val id: Int,

    @ColumnInfo(name = "list_key")
    val listKey: String,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "category_id", index = true)
    val categoryId: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Date,

    val title: String,
    val description: String?,

    val category: String,
    // Violates 1NF, but we don't need to update an offer in our database for now
    val images: List<String>,
    val size: String?,
    val location: String?
)
