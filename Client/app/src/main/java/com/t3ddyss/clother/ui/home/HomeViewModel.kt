package com.t3ddyss.clother.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.data.ErrorResponse
import com.t3ddyss.clother.data.UserRepository
import org.json.JSONObject
import retrofit2.HttpException


class HomeViewModel : ViewModel() {

    private val repository: UserRepository = UserRepository()

    // Dispatchers.IO to create a new scope
    val users = liveData {
        try {
            val usersList = repository.getUsers()
            emit(usersList)
        }

        catch (ex: Exception) {
            if (ex is HttpException) {
                Log.d("ViewModel", ex.message().toString())

                val gson = Gson()
                val type = object: TypeToken<ErrorResponse>() {}.type
                val errorResponse: ErrorResponse? = gson
                        .fromJson(ex.response()?.errorBody()?.charStream(), type)

                errorResponse?.let { Log.d("ViewModel",
                        it.message ?: "Error message is NULL") }
            }
        }
    }
}