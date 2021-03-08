package com.t3ddyss.clother.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.models.Error
import retrofit2.HttpException

fun <T> handleError(ex: HttpException): Error<T> {
    val gson = Gson()
    val type = object : TypeToken<Error<T>>() {}.type
    val response: Error<T>? = gson.fromJson(ex.response()?.errorBody()?.charStream(), type)

    return Error(response?.message)
}