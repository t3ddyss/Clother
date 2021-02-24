package com.t3ddyss.clother.ui.sign_in

import androidx.lifecycle.*
import com.t3ddyss.clother.api.Loading
import com.t3ddyss.clother.api.Resource
import com.t3ddyss.clother.data.SignInResponse
import com.t3ddyss.clother.data.User
import com.t3ddyss.clother.utilities.DEFAULT_STRING_VALUE
import com.t3ddyss.clother.utilities.EMAIL
import com.t3ddyss.clother.utilities.PASSWORD
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

class SignInViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repository by lazy { SignInRepository() }
    private val _email = savedStateHandle.getLiveData(EMAIL, DEFAULT_STRING_VALUE)
    val email: LiveData<String> = _email

    private val _password = savedStateHandle.getLiveData(PASSWORD, DEFAULT_STRING_VALUE)
    val password: LiveData<String> = _password

    private val _signInResponse = MutableLiveData<Resource<SignInResponse>>()
    val signInResponse: LiveData<Resource<SignInResponse>> = _signInResponse

    fun saveEmail(email: String) {
        savedStateHandle.set(EMAIL, email)
    }

    fun savePassword(password: String) {
        savedStateHandle.set(PASSWORD, password)
    }

    fun signInWithCredentials(email: String, password: String) {
        _signInResponse.value = Loading()
        viewModelScope.launch {
            val response = repository.signInWithCredentials(User(email = email,
            password = password))
            _signInResponse.postValue(response)
        }
    }
}