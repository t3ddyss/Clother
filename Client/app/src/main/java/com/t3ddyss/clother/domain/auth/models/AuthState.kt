package com.t3ddyss.clother.domain.auth.models

sealed class AuthState {
    object None : AuthState()
    class Authenticated(val authData: AuthData) : AuthState()
}