package com.t3ddyss.clother.domain.chat.models

sealed class CloudEvent {
    class NewMessage(val message: Message) : CloudEvent()
    class NewChat(val chat: Chat) : CloudEvent()
}
