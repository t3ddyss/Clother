package com.t3ddyss.clother.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "offers",
        foreignKeys = [ForeignKey(
                entity = Category::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("category_id"))])
data class Offer(@SerializedName("user_id") val userId: Int,
                 @SerializedName("category_id")
                 @ColumnInfo(name  = "category_id")
                 val categoryId: Int,
                 val title: String,
                 val description: String? = null,
                 val location: String? = null,
                 val image: String?) {
    @PrimaryKey var id = 0 // To exclude this property from equals() method
}
