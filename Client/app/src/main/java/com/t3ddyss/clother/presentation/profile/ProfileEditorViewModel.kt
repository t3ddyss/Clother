package com.t3ddyss.clother.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.auth.ProfileInteractor
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.Success
import com.t3ddyss.core.util.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val profileInteractor: ProfileInteractor,
) : ViewModel() {
    private var user: User? = null

    private val _avatar = MutableLiveData<String?>()
    val avatar: LiveData<String?> = _avatar

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _status = MutableLiveData<String?>()
    val status: LiveData<String?> = _status

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _applyResult = MutableLiveData<Resource<*>>()
    val applyResult: LiveData<Resource<*>> = _applyResult

    init {
        viewModelScope.launch {
            profileInteractor.observeCurrentUserInfo().first().content?.let {
                user = it
                _avatar.postValue(it.image.ifEmpty { null })
                _name.postValue(it.name)
                _status.postValue(it.details?.status)
                _isLoading.postValue(false)
            }
        }
    }

    fun updateName(updatedName: String) {
        _name.value = updatedName
    }

    fun updateStatus(updatedStatus: String) {
        _status.value = updatedStatus
    }

    fun updateAvatar(updatedAvatar: String?) {
        _avatar.value = updatedAvatar
    }

    fun onApplyClick(nameInput: String, statusInput: String) {
        val isAvatarChanged = avatar.value != user?.image?.ifEmpty { null }
        val isNameChanged = nameInput != user?.name
        val isStatusChanged = statusInput != user?.details?.status
        log("ProfileEditorViewModel.onApplyClick(): isAvatarChanged=$isAvatarChanged, isNameChanged=$isNameChanged, isStatusChanged=$isStatusChanged")

        if (isAvatarChanged || isNameChanged || isStatusChanged) {
            _isLoading.value = true
            viewModelScope.launch {
                profileInteractor.updateCurrentUserInfo(
                    name = nameInput,
                    status = statusInput,
                    avatar = avatar.value
                ).let {
                    _applyResult.postValue(it)
                }
            }
        } else {
            _applyResult.postValue(Success(Unit))
        }
    }
}