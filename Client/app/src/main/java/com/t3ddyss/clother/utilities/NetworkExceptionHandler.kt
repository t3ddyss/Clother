package com.t3ddyss.clother.utilities

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.models.common.Error
import com.t3ddyss.clother.models.common.Resource
import com.t3ddyss.clother.models.common.Success
import retrofit2.HttpException
import java.lang.IllegalStateException

inline fun <RequestResult> handleException(
        request: () -> RequestResult
): Resource<RequestResult> {
    return try {
        val result = request.invoke()
        Success(result)
    }
    catch (ex: Exception) {
        Error(ex.message)
    }
}

fun <T> handleError(ex: HttpException): Error<T> {
    val gson = Gson()
    val type = object : TypeToken<Error<*>>() {}.type

    return try {
        gson.fromJson(ex.response()?.errorBody()?.charStream(), type)
    }
    catch (ex: IllegalStateException) {
        Error(null)
    }
    catch (ex: JsonSyntaxException) {
        Error(null)
    }
}