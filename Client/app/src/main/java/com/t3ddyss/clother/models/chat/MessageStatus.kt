package com.t3ddyss.clother.models.chat

import com.google.gson.annotations.SerializedName

enum class MessageStatus {
    @SerializedName("0")
    DELIVERING,
    @SerializedName("1")
    DELIVERED,
    @SerializedName("2")
    FAILED
}