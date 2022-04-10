package com.t3ddyss.clother.domain.chat.models

import java.util.*

data class Message(
    val localId: Int,
    val serverId: Int?,
    val userId: Int,
    val userName: String,
    val createdAt: Date,
    var status: MessageStatus,
    val body: String?,
    val image: String?,
    val isIncoming: Boolean
) {
    // "It is not required that if two objects are unequal according to the equals(java.lang.Object)
    // method, then calling the hashCode method on each of the two objects must produce
    // distinct integer results"
    // https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#hashCode()
    override fun hashCode(): Int {
        return localId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (userId != other.userId) return false
        if (createdAt != other.createdAt) return false
        if (status != other.status) return false
        if (body != other.body) return false
        if (image != other.image) return false

        return true
    }
}
