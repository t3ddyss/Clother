package com.t3ddyss.clother.domain.models

import java.util.*

data class Offer(
    val id: Int,

    var userId: Int,

    val userName: String,

    val createdAt: Date,

    val title: String,
    val description: String?,
    val category: String,
    val images: List<String>,
    val size: String?,
    val location: String?
)