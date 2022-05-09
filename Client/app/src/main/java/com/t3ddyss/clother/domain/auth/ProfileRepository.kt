package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.auth.models.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeUserInfo(userId: Int): Flow<User>
}