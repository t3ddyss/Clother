package com.t3ddyss.clother.domain.offers.models

import android.net.Uri

data class MediaImage(
    val uri: Uri,
    var isSelected: Boolean = false
)