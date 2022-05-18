package com.t3ddyss.clother.util

import com.google.gson.Gson
import com.t3ddyss.clother.data.common.common.remote.models.ResponseDto
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.InfoMessage
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.util.extensions.rethrowIfCancellationException
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
        Error(throwable = ex, message = InfoMessage.StringMessage(errorText))
    } catch (exception: Exception) {
        ex.rethrowIfCancellationException()
        Error(throwable = ex)
    }
} catch (ex: Exception) {
    ex.rethrowIfCancellationException()
    Error(throwable = ex)
}
