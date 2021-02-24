package com.t3ddyss.clother.ui.password_reset

import androidx.lifecycle.*
import com.t3ddyss.clother.api.Loading
import com.t3ddyss.clother.api.Resource
import com.t3ddyss.clother.data.PasswordResetResponse
import com.t3ddyss.clother.data.User
import com.t3ddyss.clother.utilities.DEFAULT_STRING_VALUE
import com.t3ddyss.clother.utilities.EMAIL
import com.t3ddyss.clother.utilities.Event
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val repository by lazy { ResetPasswordRepository() }
    private val _email = savedStateHandle.getLiveData(EMAIL, DEFAULT_STRING_VALUE)
    val email: LiveData<String> = _email

    private val _passwordResetResponse = MutableLiveData<Event<Resource<PasswordResetResponse>>>()
    val passwordResetResponse: LiveData<Event<Resource<PasswordResetResponse>>> = _passwordResetResponse

    fun saveEmail(email: String) {
        savedStateHandle.set(EMAIL, email)
    }

    fun resetPassword(email: String) {
        _passwordResetResponse.value = Event(Loading())
        viewModelScope.launch {
            val response = repository.resetPassword(User(email = email))
            _passwordResetResponse.postValue(Event(response))
        }
    }
}