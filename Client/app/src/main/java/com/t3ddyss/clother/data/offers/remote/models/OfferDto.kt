package com.t3ddyss.clother.data.offers.remote.models

import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.data.auth.remote.models.UserDto
import java.util.*

data class OfferDto(
    val id: Int,

    val user: UserDto,

    @SerializedName("category_id")
    val categoryId: Int,

    @SerializedName("created_at")
    val createdAt: Date,

    val title: String,
    val description: String?,
    val category: String,
    val images: List<String>,
    val size: String?,
    val location: String?
)