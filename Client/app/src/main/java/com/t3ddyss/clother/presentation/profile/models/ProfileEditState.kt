package com.t3ddyss.clother.presentation.profile.models

import com.t3ddyss.clother.domain.auth.ProfileInteractor

sealed interface ProfileEditState {
    object Loading : ProfileEditState
    data class ValidationError(val causes: List<ProfileInteractor.ProfileParam>) : ProfileEditState
    object Success : ProfileEditState
    object Error : ProfileEditState
}