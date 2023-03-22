package com.t3ddyss.clother.presentation.profile.models

import com.t3ddyss.clother.domain.auth.models.User

data class UserInfoChange(
    val current: User,
    val updated: User
) {
    val isChanged get() = updated != current
}
