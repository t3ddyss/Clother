package com.t3ddyss.clother.domain.chat.models

sealed class Event {
    object Connect : Event()
    object Disconnect : Event()
    class NewMessage(val message: Message) : Event()
    class NewChat(val chat: Chat) : Event()
}
