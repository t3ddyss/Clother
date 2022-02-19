package com.t3ddyss.clother.util

import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Success
import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Loading(data))

        try {
            saveFetchResult(fetch())
            query().map { Success(it) }
        } catch (throwable: Throwable) {
            query().map { Error<ResultType>(throwable, null) }
        }

    } else {
        query().map { Success(it) }
    }

    emitAll(flow)
}