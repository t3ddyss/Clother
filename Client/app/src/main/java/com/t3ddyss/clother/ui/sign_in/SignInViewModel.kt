package com.t3ddyss.clother.ui.sign_in

import androidx.lifecycle.*
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.models.Loading
import com.t3ddyss.clother.models.ResponseState
import com.t3ddyss.clother.models.SignInResponse
import com.t3ddyss.clother.models.User
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
        private val repository: UsersRepository,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _email = savedStateHandle.getLiveData(EMAIL, DEFAULT_STRING_VALUE)
    val email: LiveData<String> = _email

    private val _password = savedStateHandle.getLiveData(PASSWORD, DEFAULT_STRING_VALUE)
    val password: LiveData<String> = _password

    private val _signInResponse = MutableLiveData<ResponseState<SignInResponse>>()
    val signInResponse: LiveData<ResponseState<SignInResponse>> = _signInResponse

    fun saveEmail(email: String) {
        savedStateHandle.set(EMAIL, email)
    }

    fun savePassword(password: String) {
        savedStateHandle.set(PASSWORD, password)
    }

    fun signInWithCredentials(email: String, password: String) {
        _signInResponse.value = Loading()
        viewModelScope.launch {
            val response = repository.signInWithCredentials(
                User(email = email,
            password = password)
            )
            _signInResponse.postValue(response)
        }
    }
}