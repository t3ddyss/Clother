package com.t3ddyss.clother.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.models.domain.Error
import com.t3ddyss.clother.models.domain.Failed
import com.t3ddyss.clother.models.domain.Resource
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

inline fun <ResultType> handleNetworkException(
        request: () -> Resource<ResultType>
): Resource<ResultType> = try {
        request.invoke()
    } catch (ex: HttpException) {
        handleHttpException(ex)
    } catch (ex: ConnectException) {
        Failed()
    } catch (ex: SocketTimeoutException) {
        Error(null)
    }

fun <T> handleHttpException(ex: HttpException): Error<T> {
    val gson = Gson()
    val type = object : TypeToken<Error<*>>() {}.type

    return try {
        gson.fromJson(ex.response()?.errorBody()?.charStream(), type)
    } catch (ex: Exception) {
        Error(null)
    }
}