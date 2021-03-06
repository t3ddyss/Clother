package com.t3ddyss.clother.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.data.SignUpResponse
import com.t3ddyss.clother.data.UserRepository
import retrofit2.HttpException


class HomeViewModel : ViewModel() {

//    private val repository by lazy { UserRepository() }
//
//    // +Dispatchers.IO to create a new scope
//    val users = liveData(viewModelScope.coroutineContext) {
//        try {
//            val usersList = repository.getUsers()
//            emit(usersList)
//        } catch (ex: Exception) {
//            if (ex is HttpException) {
//                Log.d("ViewModel", ex.message().toString())
//
//                val gson = Gson()
//                val type = object : TypeToken<SignUpResponse>() {}.type
//                val signUpResponse: SignUpResponse? = gson
//                        .fromJson(ex.response()?.errorBody()?.charStream(), type)
//
//                signUpResponse?.let {
//                    Log.d("ViewModel",
//                            null ?: "Error message is NULL")
//                }
//            }
//        }
//    }
}