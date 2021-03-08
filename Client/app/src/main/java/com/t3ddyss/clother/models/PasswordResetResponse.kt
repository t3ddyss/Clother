package com.t3ddyss.clother.models

data class PasswordResetResponse(val message: String?,
                                 var email: String? = null)