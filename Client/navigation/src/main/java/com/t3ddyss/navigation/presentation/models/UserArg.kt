package com.t3ddyss.navigation.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserArg(
    val id: Int,
    val name: String
) : Parcelable
