package com.t3ddyss.core.domain.models

sealed class Resource<T>(
    val content: T? = null,
    val message: String? = null
)

class Loading<T>(content: T? = null) : Resource<T>(content)

class Success<T>(content: T) : Resource<T>(content)

class Error<T>(val throwable: Throwable? = null, message: String? = null) : Resource<T>(null, message)
