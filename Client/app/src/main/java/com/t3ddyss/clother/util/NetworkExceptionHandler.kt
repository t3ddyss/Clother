package com.t3ddyss.clother.util

import com.google.gson.Gson
import com.t3ddyss.clother.data.common.remote.models.ResponseDto
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.util.rethrowIfCancellationException
import retrofit2.HttpException

suspend inline fun <ResultType> handleHttpException(
    crossinline request: suspend () -> ResultType
): Resource<ResultType> = try {
    Success(request.invoke())
} catch (ex: HttpException) {
    val gson = Gson()
    try {
        val errorText = gson.fromJson(
            ex.response()?.errorBody()?.charStream(),
            ResponseDto::class.java
        ).message
        Error(ex, errorText)
    } catch (exception: Exception) {
        ex.rethrowIfCancellationException()
        Error(ex, null)
    }
} catch (ex: Exception) {
    ex.rethrowIfCancellationException()
    Error(ex, null)
}
