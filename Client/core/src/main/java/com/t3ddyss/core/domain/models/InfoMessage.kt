package com.t3ddyss.core.domain.models

import androidx.annotation.StringRes

sealed class InfoMessage {
    class StringMessage(val message: String?): InfoMessage()
    class StringResMessage(@StringRes val messageRes: Int): InfoMessage()
}