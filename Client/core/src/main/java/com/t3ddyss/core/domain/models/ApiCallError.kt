package com.t3ddyss.core.domain.models

sealed interface ApiCallError {
    object ConnectionError : ApiCallError
    object UnknownError : ApiCallError
}