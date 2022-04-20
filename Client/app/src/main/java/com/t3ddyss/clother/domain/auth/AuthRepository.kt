package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.common.common.models.Response

interface AuthRepository {
    val authData: AuthData
    suspend fun signUp(name: String, email: String, password: String): Response
    suspend fun signIn(email: String, password: String): AuthData
    suspend fun resetPassword(email: String): Response
    suspend fun saveAuthData(authData: AuthData)
    suspend fun deleteAllUserData()
}