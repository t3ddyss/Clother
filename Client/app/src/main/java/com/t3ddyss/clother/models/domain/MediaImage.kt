package com.t3ddyss.clother.models.domain

import android.net.Uri

data class MediaImage(
    val uri: Uri,
    var isSelected: Boolean = false
)