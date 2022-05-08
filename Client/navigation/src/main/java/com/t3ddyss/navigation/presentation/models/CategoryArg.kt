package com.t3ddyss.navigation.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryArg(
    val id: Int,
    val title: String,
    val isLastLevel: Boolean
) : Parcelable
