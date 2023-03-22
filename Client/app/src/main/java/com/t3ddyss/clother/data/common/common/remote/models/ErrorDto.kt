package com.t3ddyss.clother.data.common.common.remote.models

import com.google.gson.annotations.SerializedName

data class ErrorDto<T: Enum<*>>(
    @SerializedName("error")
    val type: T
)