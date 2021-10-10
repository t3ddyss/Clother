package com.t3ddyss.clother.models.domain

sealed class AuthState {
    object None : AuthState()
    object Refreshing : AuthState()
    class Authenticated(val accessToken: String) : AuthState()
}