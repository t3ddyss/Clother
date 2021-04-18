package com.t3ddyss.clother.models.offers

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.util.*

// TODO store images in a separate table like on server-side
@Entity(tableName = "offer",
        primaryKeys = ["id", "list_key"],
        foreignKeys = [ForeignKey(
                entity = Category::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("category_id"))])
data class Offer(var id: Int = 0,

                 @ColumnInfo(name = "list_key")
                 var listKey: String = "offers",

                 @SerializedName("user_id")
                 @ColumnInfo(name = "user_id" )
                 var userId: Int = 0,

                 @SerializedName("category_id")
                 @ColumnInfo(name = "category_id", index = true)
                 var categoryId: Int = 0,

                 @SerializedName("created_at")
                 @ColumnInfo(name = "created_at")
                 val createdAt: Date,

                 var title: String = "",
                 var description: String? = null,

                 @SerializedName("user_name")
                 @ColumnInfo(name = "user_name")
                 var userName: String = "",

                 var category: String = "",
                 var images: List<String> = listOf(),
                 val size: String? = null,
                 var location: String? = null
)
