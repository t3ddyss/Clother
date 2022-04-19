package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Event
import kotlinx.coroutines.flow.Flow

interface ChatListenerRepository {
    fun observeEvents(): Flow<Event>
}