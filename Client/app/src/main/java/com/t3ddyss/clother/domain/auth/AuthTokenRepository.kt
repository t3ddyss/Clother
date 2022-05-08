package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.auth.models.UserAuthData
import kotlinx.coroutines.flow.Flow

interface AuthTokenRepository {
    val tokenStateFlow: Flow<UserAuthData?>
}