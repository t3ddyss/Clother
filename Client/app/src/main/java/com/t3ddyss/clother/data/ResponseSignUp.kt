package com.t3ddyss.clother.data

data class ResponseSignUp(var isSuccessful: Boolean, val message: String?, var email: String? = null)