package com.t3ddyss.clother.presentation.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.SignInError
import com.t3ddyss.clother.util.Event
import com.t3ddyss.clother.util.toEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authInteractor: AuthInteractor
) : ViewModel() {

    private val _state = MutableSharedFlow<SignInState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val state = _state.asSharedFlow().distinctUntilChanged()

    private val _error = MutableSharedFlow<Event<SignInError>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val error = _error.asSharedFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authInteractor.validateParameters(email = email)
                .tap {
                    _state.emit(SignInState.Loading)
                    authInteractor.signIn(email, password)
                        .tap {
                            _state.emit(SignInState.Success)
                        }
                        .tapLeft { error ->
                            _state.emit(SignInState.Error)
                            _error.emit(error.toEvent())
                        }
                }
                .tapLeft { errors ->
                    _state.emit(SignInState.ValidationError(errors))
                }
        }
    }
}