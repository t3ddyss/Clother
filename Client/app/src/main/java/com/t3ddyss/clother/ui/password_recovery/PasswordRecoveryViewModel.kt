package com.t3ddyss.clother.ui.password_recovery

import androidx.lifecycle.*
import com.t3ddyss.clother.data.UsersRepository
import com.t3ddyss.clother.models.auth.AuthResponse
import com.t3ddyss.clother.models.common.Loading
import com.t3ddyss.clother.models.common.Resource
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

    private val _passwordResetResponse = MutableLiveData<Event<Resource<AuthResponse>>>()
    val passwordResetResponse: LiveData<Event<Resource<AuthResponse>>> = _passwordResetResponse

    fun saveEmail(email: String) {
        savedStateHandle.set(EMAIL, email)
    }

    fun resetPassword(email: String) {
        _passwordResetResponse.value = Event(Loading())
        viewModelScope.launch {
            val response = repository.resetPassword(email = email)
            _passwordResetResponse.postValue(Event(response))
        }
    }
}