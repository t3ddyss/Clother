package com.t3ddyss.clother.utilities

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.models.common.Error
import retrofit2.HttpException
import java.lang.IllegalStateException

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