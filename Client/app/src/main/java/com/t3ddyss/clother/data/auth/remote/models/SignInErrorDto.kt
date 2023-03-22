package com.t3ddyss.clother.data.auth.remote.models

import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.data.common.common.remote.models.ErrorDto

typealias SignInErrorDto = ErrorDto<SignInErrorType>

enum class SignInErrorType {
    @SerializedName("email_not_verified")
    EMAIL_NOT_VERIFIED,
    @SerializedName("invalid_credentials")
    INVALID_CREDENTIALS
}