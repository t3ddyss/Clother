package com.t3ddyss.clother.ui.password_recovery

import androidx.lifecycle.*
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.models.Loading
import com.t3ddyss.clother.models.PasswordResetResponse
import com.t3ddyss.clother.models.ResponseState
import com.t3ddyss.clother.models.User
import com.t3ddyss.clother.utilities.DEFAULT_STRING_VALUE
import com.t3ddyss.clother.utilities.EMAIL
import com.t3ddyss.clother.utilities.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(
        private val repository: UsersRepository,
        private val savedStateHandle: SavedStateHandle): ViewModel() {
    private val _email = savedStateHandle.getLiveData(EMAIL, DEFAULT_STRING_VALUE)
    val email: LiveData<String> = _email

    private val _passwordResetResponse = MutableLiveData<Event<ResponseState<PasswordResetResponse>>>()
    val passwordResetResponse: LiveData<Event<ResponseState<PasswordResetResponse>>> = _passwordResetResponse

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