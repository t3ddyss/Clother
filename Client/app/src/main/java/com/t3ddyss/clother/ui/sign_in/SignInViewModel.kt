package com.t3ddyss.clother.ui.sign_in

import androidx.lifecycle.*
import com.t3ddyss.clother.data.UsersRepository
import com.t3ddyss.clother.models.auth.AuthTokens
import com.t3ddyss.clother.models.Loading
import com.t3ddyss.clother.models.Resource
import com.t3ddyss.clother.utilities.DEFAULT_STRING_VALUE
import com.t3ddyss.clother.utilities.EMAIL
import com.t3ddyss.clother.utilities.PASSWORD
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

    private val _signInResponse = MutableLiveData<Resource<AuthTokens>>()
    val authTokens: LiveData<Resource<AuthTokens>> = _signInResponse

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
                    email = email,
                    password = password
            )
            _signInResponse.postValue(response)
        }
    }
}