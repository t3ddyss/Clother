package com.t3ddyss.clother.models

import androidx.room.*
import com.google.gson.annotations.SerializedName

@Entity(tableName = "offer",
        foreignKeys = [ForeignKey(
                entity = Category::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("category_id"))])
data class Offer(@PrimaryKey var id: Int = 0,

                 @SerializedName("user_id")
                 @ColumnInfo(name = "user_id" )
                 var userId: Int = 0,

                 @SerializedName("category_id")
                 @ColumnInfo(name = "category_id", index = true)
                 var categoryId: Int = 0,

                 @SerializedName("created_at")
                 @ColumnInfo(name = "created_at")
                 var createdAt: String = "",

                 var title: String = "",
                 var description: String? = null,

                 @SerializedName("user_name")
                 @ColumnInfo(name = "user_name")
                 var userName: String = "",

                 var category: String = "",
                 var images: List<String> = listOf(),
                 @Ignore val size: Map<String, String>? = null,
                 var location: String? = null
)
