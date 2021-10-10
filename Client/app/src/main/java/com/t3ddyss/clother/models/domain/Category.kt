package com.t3ddyss.clother.models.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: Int,
    val title: String,
    val isLastLevel: Boolean
) : Parcelable
