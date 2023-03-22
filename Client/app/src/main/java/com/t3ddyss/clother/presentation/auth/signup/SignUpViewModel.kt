package com.t3ddyss.clother.presentation.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.SignUpError
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
class SignUpViewModel @Inject constructor(
    private val authInteractor: AuthInteractor
) : ViewModel() {

    private val _state = MutableSharedFlow<SignUpState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val state = _state.asSharedFlow().distinctUntilChanged()

    private val _error = MutableSharedFlow<Event<SignUpError>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val error = _error.asSharedFlow()

    fun createUserWithCredentials(name: String, email: String, password: String) {
        viewModelScope.launch {
            authInteractor.validateParameters(name, email, password)
                .tap {
                    _state.emit(SignUpState.Loading)
                    authInteractor.signUp(name, email, password)
                        .tapLeft {
                            _state.emit(SignUpState.Error)
                            _error.emit(it.toEvent())
                        }
                        .tap {
                            _state.emit(SignUpState.Success)
                        }
                }
                .tapLeft { errors ->
                    _state.emit(SignUpState.ValidationError(errors.all))
                }
        }
    }
}