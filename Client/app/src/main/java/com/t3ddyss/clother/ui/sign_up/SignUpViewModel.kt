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

    private val _name = savedStateHandle.getLiveData(NAME, DEFAULT_STRING_VALUE)
    val name: LiveData<String> = _name

    private val _email = savedStateHandle.getLiveData(EMAIL, DEFAULT_STRING_VALUE)
    val email: LiveData<String> = _email

    private val _password = savedStateHandle.getLiveData(PASSWORD, DEFAULT_STRING_VALUE)
    val password: LiveData<String> = _password

    private val _signUpResult = MutableLiveData<Event<Resource<Response>>>()
    val signUpResult: LiveData<Event<Resource<Response>>> = _signUpResult

    fun createUserWithCredentials(name: String, email: String, password: String) {
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

        _name.value = DEFAULT_STRING_VALUE
        _email.value = DEFAULT_STRING_VALUE
        _password.value = DEFAULT_STRING_VALUE
    }
}