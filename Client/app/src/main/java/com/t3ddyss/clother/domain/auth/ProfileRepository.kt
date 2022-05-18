package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.core.domain.models.Resource
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ProfileRepository {
    fun observeCurrentUserInfo(): Flow<Resource<User>>
    fun observeUserInfo(userId: Int): Flow<Resource<User>>
    suspend fun updateCurrentUserInfo(name: String, status: String, avatar: File?): User
}