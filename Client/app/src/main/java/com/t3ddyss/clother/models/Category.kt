package com.t3ddyss.clother.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "categories",
        foreignKeys = [ForeignKey(
                entity = Category::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("parent_id"))])
data class Category(@PrimaryKey val id: Int,
                    @SerializedName("parent_id")
                    @ColumnInfo(name="parent_id", index = true)
                    val parentId: Int?,
                    val title: String,
                    @SerializedName("last_level")
                    @ColumnInfo(name = "last_level")
                    val isLastLevel: Boolean)