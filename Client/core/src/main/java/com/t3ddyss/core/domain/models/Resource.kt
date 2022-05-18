package com.t3ddyss.core.domain.models

sealed class Resource<T>(
    val content: T? = null,
    val message: InfoMessage? = null
)

class Loading<T>(content: T? = null) : Resource<T>(content)

class Success<T>(content: T) : Resource<T>(content)

class Error<T>(
    val throwable: Throwable? = null,
    content: T? = null,
    message: InfoMessage? = null
) : Resource<T>(content, message)
