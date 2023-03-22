package com.t3ddyss.clother.data.auth.remote.models

import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.data.common.common.remote.models.ErrorDto

typealias SignUpErrorDto = ErrorDto<SignUpErrorType>

enum class SignUpErrorType {
    @SerializedName("email_occupied")
    EMAIL_OCCUPIED
}
