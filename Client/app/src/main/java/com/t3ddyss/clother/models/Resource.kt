package com.t3ddyss.clother.models

sealed class Resource<T>(
        val content: T? = null,
        val message: String? = null
)

class Loading<T>(content: T? = null) : Resource<T>(content)
class Success<T>(content: T) : Resource<T>(content)
class Error<T>(message: String?, content: T? = null) : Resource<T>(content, message)
class Failed<T> : Resource<T>()
