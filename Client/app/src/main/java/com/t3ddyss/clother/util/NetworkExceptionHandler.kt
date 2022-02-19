package com.t3ddyss.clother.util

import com.google.gson.Gson
import com.t3ddyss.clother.models.dto.ResponseDto
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Resource
import retrofit2.HttpException

inline fun <ResultType> handleNetworkError(
    request: () -> Resource<ResultType>
): Resource<ResultType> = try {
    request.invoke()
} catch (ex: HttpException) {
    val gson = Gson()
    try {
        val errorText = gson.fromJson(
            ex.response()?.errorBody()?.charStream(),
            ResponseDto::class.java
        ).message
        Error(ex, errorText)
    } catch (exception: Exception) {
        Error(ex, null)
    }
} catch (ex: Exception) {
    Error(ex, null)
}
