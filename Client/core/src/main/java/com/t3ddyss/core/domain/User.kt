package com.t3ddyss.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val email: String = "",
    val image: String = ""
) : Parcelable {
    // Overridden for toolbar label
    override fun toString(): String {
        return name
    }
}