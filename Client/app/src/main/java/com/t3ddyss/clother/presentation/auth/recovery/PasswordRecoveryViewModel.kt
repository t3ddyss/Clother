package com.t3ddyss.clother.presentation.auth.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.ResetPasswordError
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
class PasswordRecoveryViewModel @Inject constructor(
    private val authInteractor: AuthInteractor
) : ViewModel() {

    private val _state = MutableSharedFlow<PasswordRecoveryState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val state = _state.asSharedFlow().distinctUntilChanged()

    private val _error = MutableSharedFlow<Event<ResetPasswordError>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val error = _error.asSharedFlow()

    fun resetPassword(email: String) {
        viewModelScope.launch {
            authInteractor.validateParameters(email = email)
                .tap {
                    _state.emit(PasswordRecoveryState.Loading)
                    authInteractor.resetPassword(email)
                        .tap {
                            _state.emit(PasswordRecoveryState.Success)
                        }
                        .tapLeft { error ->
                            _state.emit(PasswordRecoveryState.Error)
                            _error.emit(error.toEvent())
                        }
                }
                .tapLeft { errors ->
                    _state.emit(PasswordRecoveryState.ValidationError(errors))
                }
        }
    }
}