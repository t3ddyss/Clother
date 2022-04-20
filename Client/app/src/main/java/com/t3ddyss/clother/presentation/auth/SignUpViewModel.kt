package com.t3ddyss.clother.presentation.auth

import androidx.lifecycle.*
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.common.common.models.Response
import com.t3ddyss.clother.util.Event
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.util.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _name =
        savedStateHandle.getLiveData(SavedStateHandleKeys.NAME, "")
    val name: LiveData<String> = _name

    private val _email =
        savedStateHandle.getLiveData(SavedStateHandleKeys.EMAIL, "")
    val email: LiveData<String> = _email

    private val _password =
        savedStateHandle.getLiveData(SavedStateHandleKeys.PASSWORD, "")
    val password: LiveData<String> = _password
    
    private val _nameError = MutableLiveData<Boolean>()
    val nameError: LiveData<Boolean> = _nameError
    
    private val _emailError = MutableLiveData<Boolean>()
    val emailError: LiveData<Boolean> = _emailError
    
    private val _passwordError = MutableLiveData<Boolean>()
    val passwordError: LiveData<Boolean> = _passwordError

    private val _signUpResult = MutableLiveData<Event<Resource<Response>>>()
    val signUpResult: LiveData<Event<Resource<Response>>> = _signUpResult

    fun createUserWithCredentials(name: String, email: String, password: String) {
        if (!StringUtils.isValidName(name)) {
            _nameError.value = true
            return
        }
        _nameError.value = false

        if (!StringUtils.isValidEmail(email)) {
            _emailError.value = true
            return
        }
        _emailError.value = false

        if (!StringUtils.isValidPassword(password)) {
            _passwordError.value = true
            return
        }
        _passwordError.value = false

        _signUpResult.value = Event(Loading())
        viewModelScope.launch {
            val response = authInteractor.signUp(name, email, password)
            _signUpResult.postValue(Event(response))
        }
    }

    fun saveName(name: String) {
        savedStateHandle[SavedStateHandleKeys.NAME] = name
    }

    fun saveEmail(email: String) {
        savedStateHandle[SavedStateHandleKeys.EMAIL] = email
    }

    fun savePassword(password: String) {
        savedStateHandle[SavedStateHandleKeys.PASSWORD] = password
    }

    fun clearCredentials() {
        savedStateHandle.remove<String>(SavedStateHandleKeys.NAME)
        savedStateHandle.remove<String>(SavedStateHandleKeys.EMAIL)
        savedStateHandle.remove<String>(SavedStateHandleKeys.PASSWORD)

        _name.value = ""
        _email.value = ""
        _password.value = ""
    }
}