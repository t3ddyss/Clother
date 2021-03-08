package com.t3ddyss.clother.models

sealed class ResponseState<T>(
        val content: T? = null,
        val message: String? = null
)

class Loading<T>(content: T? = null) : ResponseState<T>(content)
class Success<T>(content: T) : ResponseState<T>(content)
class Error<T>(message: String?, content: T? = null) : ResponseState<T>(content, message)
class Failed<T> : ResponseState<T>()
