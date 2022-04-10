package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.domain.common.models.Response
import com.t3ddyss.core.domain.models.Resource
import kotlinx.coroutines.flow.StateFlow

interface AuthInteractor {
    val authState: StateFlow<AuthState>
    suspend fun signUp(name: String, email: String, password: String): Resource<Response>
    suspend fun signIn(email: String, password: String): Resource<AuthData>
    suspend fun resetPassword(email: String): Resource<Response>
}