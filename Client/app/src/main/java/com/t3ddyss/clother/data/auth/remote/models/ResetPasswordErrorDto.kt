package com.t3ddyss.clother.data.auth.remote.models

import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.data.common.common.remote.models.ErrorDto

typealias ResetPasswordErrorDto = ErrorDto<ResetPasswordErrorType>

enum class ResetPasswordErrorType {
    @SerializedName("user_not_found")
    USER_NOT_FOUND
}