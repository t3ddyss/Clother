package com.t3ddyss.clother.util

import com.google.gson.Gson
import com.t3ddyss.clother.data.remote.dto.ResponseDto
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.Success
import retrofit2.HttpException
import java.util.concurrent.CancellationException

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
        if (exception is CancellationException) throw exception
        Error(ex, null)
    }
} catch (ex: Exception) {
    if (ex is CancellationException) throw ex
    Error(ex, null)
}
