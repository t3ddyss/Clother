package com.t3ddyss.clother.ui.sign_up

import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.R
import com.t3ddyss.clother.data.ResponseSignUp
import com.t3ddyss.clother.data.LoadingStates
import com.t3ddyss.clother.data.User
import com.t3ddyss.clother.utilities.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

class SignUpViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val PASSWORD = "password"
        private const val DEFAULT_VALUE = ""
    }

    private val repository by lazy { SignUpRepository() }
    private val _name = savedStateHandle.getLiveData(NAME, DEFAULT_VALUE)
    val name: LiveData<String> = _name

    private val _email = savedStateHandle.getLiveData(EMAIL, DEFAULT_VALUE)
    val email: LiveData<String> = _email

    private val _password = savedStateHandle.getLiveData(PASSWORD, DEFAULT_VALUE)
    val password: LiveData<String> = _password

    private val _responseSignUp = MutableLiveData<Event<ResponseSignUp>>()
    val responseSignUp: LiveData<Event<ResponseSignUp>> = _responseSignUp

    private val _loadingState = MutableLiveData<LoadingStates>()
    val loadingState: LiveData<LoadingStates> = _loadingState

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
        _name.value = DEFAULT_VALUE
        _email.value = DEFAULT_VALUE
        _password.value = DEFAULT_VALUE
    }

    fun createUserWithCredentials(name: String, email: String, password: String) {
        _loadingState.value = LoadingStates.LOADING
        viewModelScope.launch {
            try {
                val response = repository.createUser(User(name, email, password))
                _responseSignUp.postValue(Event(response.also { it.email = email
                                                                it.isSuccessful = true}))
                _loadingState.postValue(LoadingStates.LOADED)
            }

            catch (ex: HttpException) {
                val gson = Gson()
                val type = object: TypeToken<ResponseSignUp>() {}.type
                val response: ResponseSignUp? = gson
                        .fromJson(ex.response()?.errorBody()?.charStream(), type)
                _responseSignUp.postValue(Event(
                        response ?: ResponseSignUp(false, null)))
                _loadingState.postValue(LoadingStates.LOADED)
            }

            catch (ex: SocketTimeoutException) {
                _loadingState.postValue(LoadingStates.FAILED_TO_LOAD)
            }
        }
    }
}