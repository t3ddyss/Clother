package com.t3ddyss.clother.domain.auth

import android.net.Uri
import arrow.core.Either
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.auth.models.UserInfoState
import com.t3ddyss.core.domain.models.ApiCallError
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeCurrentUserInfo(): Flow<UserInfoState>
    fun observeUserInfo(userId: Int): Flow<UserInfoState>
    suspend fun updateCurrentUserInfo(name: String, status: String, avatar: Uri?): Either<ApiCallError, User>
}