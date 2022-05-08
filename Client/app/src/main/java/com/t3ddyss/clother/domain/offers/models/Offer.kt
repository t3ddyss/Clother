package com.t3ddyss.clother.domain.offers.models

import com.t3ddyss.clother.domain.auth.models.User
import java.util.*

data class Offer(
    val id: Int,
    val user: User,
    val createdAt: Date,
    val title: String,
    val description: String?,
    val category: String,
    val images: List<String>,
    val size: String?,
    val location: String?
)