package com.t3ddyss.clother.utilities

import com.t3ddyss.clother.models.Error
import com.t3ddyss.clother.models.Loading
import com.t3ddyss.clother.models.Success
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
            query().map { Error(message = throwable.message, data) }
        }

    } else {
        query().map { Success(it) }
    }

    emitAll(flow)
}