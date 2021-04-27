package com.t3ddyss.clother.models.dto

import com.google.gson.annotations.SerializedName
import java.util.*

data class OfferDto(
    val id: Int,

    @SerializedName("category_id")
    var categoryId: Int,

    @SerializedName("user_id")
    var userId: Int,

    @SerializedName("user_name")
    val userName: String,

    @SerializedName("created_at")
    val createdAt: Date,

    val title: String,
    val description: String?,
    val category: String,
    val images: List<String>,
    val size: String?,
    val location: String?
)