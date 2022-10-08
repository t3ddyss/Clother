package com.t3ddyss.clother.domain.auth.models

import android.net.Uri

data class User(
    val id: Int,
    val name: String,
    val image: Uri? = null,
    val details: UserDetails? = null
)