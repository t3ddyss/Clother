package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.models.AuthData
import kotlinx.coroutines.flow.Flow
import okhttp3.Authenticator

interface AuthTokenRepository : Authenticator {
    val tokenStateFlow: Flow<AuthData?>
}