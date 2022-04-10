package com.t3ddyss.clother.presentation.auth

import androidx.lifecycle.*
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _email = savedStateHandle
        .getLiveData(SavedStateHandleKeys.EMAIL, "")
    val email: LiveData<String> = _email

    private val _password = savedStateHandle
        .getLiveData(SavedStateHandleKeys.PASSWORD, "")
    val password: LiveData<String> = _password

    private val _signInResult = MutableLiveData<Resource<*>>()
    val signInResult: LiveData<Resource<*>> = _signInResult

    fun signInWithCredentials(email: String, password: String) {
        _signInResult.value = Loading(null)
        viewModelScope.launch {
            val response = authInteractor.signIn(
                email = email,
                password = password
            )

            _signInResult.postValue(response)
        }
    }

    fun saveEmail(email: String) {
        savedStateHandle.set(SavedStateHandleKeys.EMAIL, email)
    }

    fun savePassword(password: String) {
        savedStateHandle.set(SavedStateHandleKeys.PASSWORD, password)
    }
}