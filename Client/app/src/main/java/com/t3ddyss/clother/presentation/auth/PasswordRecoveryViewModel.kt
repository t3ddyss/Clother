package com.t3ddyss.clother.presentation.auth

import androidx.lifecycle.*
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.models.Response
import com.t3ddyss.clother.util.Event
import com.t3ddyss.core.domain.models.Loading
import com.t3ddyss.core.domain.models.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _email =
        savedStateHandle.getLiveData(SavedStateHandleKeys.EMAIL, "")
    val email: LiveData<String> = _email

    private val _passwordRecoveryResult = MutableLiveData<Event<Resource<Response>>>()
    val passwordRecoveryResult: LiveData<Event<Resource<Response>>> = _passwordRecoveryResult

    fun resetPassword(email: String) {
        _passwordRecoveryResult.value = Event(Loading())
        viewModelScope.launch {
            val response = authInteractor.resetPassword(email = email)
            _passwordRecoveryResult.postValue(Event(response))
        }
    }

    fun saveEmail(email: String) {
        savedStateHandle.set(SavedStateHandleKeys.EMAIL, email)
    }
}