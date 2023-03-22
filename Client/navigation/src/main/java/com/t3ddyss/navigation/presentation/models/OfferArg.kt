package com.t3ddyss.navigation.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OfferArg(
    val id: Int,
    val user: UserArg
) : Parcelable