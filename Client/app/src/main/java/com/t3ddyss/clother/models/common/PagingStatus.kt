package com.t3ddyss.clother.models.common

sealed class LoadResult {
    class Success(val isEndOfPaginationReached: Boolean) : LoadResult()
    class Error(val exception: Exception) : LoadResult()
}

enum class LoadType {
    REFRESH,
    APPEND
}