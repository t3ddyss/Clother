package com.t3ddyss.clother.domain.chat.models

import com.t3ddyss.core.domain.models.ApiCallError

sealed interface ChatsState {
    val chats: List<Chat>

    data class Cache(override val chats: List<Chat>) : ChatsState
    data class Fetched(override val chats: List<Chat>) : ChatsState
    data class Error(override val chats: List<Chat>, val error: ApiCallError) : ChatsState
}