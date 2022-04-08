package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.auth.models.AuthData
import kotlinx.coroutines.flow.Flow

interface AuthTokenRepository {
    val tokenStateFlow: Flow<AuthData?>
}