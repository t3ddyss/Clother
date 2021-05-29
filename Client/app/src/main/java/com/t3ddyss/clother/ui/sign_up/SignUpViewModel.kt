package com.t3ddyss.clother.ui.sign_up

import androidx.lifecycle.*
import com.t3ddyss.clother.data.UsersRepository
import com.t3ddyss.clother.models.domain.Loading
import com.t3ddyss.clother.models.domain.Resource
import com.t3ddyss.clother.models.domain.Response
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: UsersRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _name = savedStateHandle.getLiveData(NAME, "")
    val name: LiveData<String> = _name

    private val _email = savedStateHandle.getLiveData(EMAIL, "")
    val email: LiveData<String> = _email

    private val _password = savedStateHandle.getLiveData(PASSWORD, "")
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

        if (!name.validateName()) {
            _nameError.value = true
            return
        }
        _nameError.value = false

        if (!email.validateEmail()) {
            _emailError.value = true
            return
        }
        _emailError.value = false

        if (!password.validatePassword()) {
            _passwordError.value = true
            return
        }
        _passwordError.value = false

        _signUpResult.value = Event(Loading())
        viewModelScope.launch {
            val response = repository.createUser(name, email, password)
            _signUpResult.postValue(Event(response))
        }
    }

    fun saveName(name: String) {
        savedStateHandle.set(NAME, name)
    }

    fun saveEmail(email: String) {
        savedStateHandle.set(EMAIL, email)
    }

    fun savePassword(password: String) {
        savedStateHandle.set(PASSWORD, password)
    }

    fun clearCredentials() {
        savedStateHandle.remove<String>(NAME)
        savedStateHandle.remove<String>(EMAIL)
        savedStateHandle.remove<String>(PASSWORD)

        _name.value = ""
        _email.value = ""
        _password.value = ""
    }
}