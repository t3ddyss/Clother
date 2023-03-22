package com.t3ddyss.clother.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.auth.ProfileInteractor
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.presentation.profile.models.ProfileEditState
import com.t3ddyss.clother.presentation.profile.models.UserInfoChange
import com.t3ddyss.clother.util.Event
import com.t3ddyss.clother.util.toEvent
import com.t3ddyss.core.domain.models.ApiCallError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val profileInteractor: ProfileInteractor
) : ViewModel() {

    private val _change = MutableSharedFlow<UserInfoChange>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val change = _change.asSharedFlow().distinctUntilChanged()

    private val _state = MutableSharedFlow<ProfileEditState>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val state = _state.asSharedFlow().distinctUntilChanged()

    private val _error = MutableSharedFlow<Event<ApiCallError>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val error = _error.asSharedFlow()

    init {
        viewModelScope.launch {
            val user = profileInteractor.observeCurrentUserInfo().first().user
            _change.emit(UserInfoChange(user, user))
        }
    }

    fun updateName(name: String) = update {
        copy(name = name)
    }

    fun updateStatus(status: String) = update {
        copy(details = details?.copy(status = status))
    }

    fun updateAvatar(avatar: Uri) = update {
        copy(image = avatar)
    }

    fun removeAvatar() = update {
        copy(image = null)
    }

    fun onApplyClick() {
        viewModelScope.launch {
            val updated = change.first().updated
            val name = updated.name
            val status = updated.details?.status.orEmpty()
            val avatar = updated.image
            profileInteractor.validateParameters(name, status)
                .tap {
                    _state.emit(ProfileEditState.Loading)
                    profileInteractor.updateCurrentUserInfo(name, status, avatar)
                        .tap {
                            _state.emit(ProfileEditState.Success)
                        }
                        .tapLeft {
                            _state.emit(ProfileEditState.Error)
                            _error.emit(it.toEvent())
                        }
                }
                .tapLeft { causes ->
                    _state.emit(ProfileEditState.ValidationError(causes))
                }
        }
    }

    private fun update(update: User.() -> User) {
        viewModelScope.launch {
            val previous = change.first()
            _change.emit(
                previous.copy(
                    updated = previous.updated.update()
                )
            )
        }
    }
}