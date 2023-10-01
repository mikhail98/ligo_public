package com.ligo.google.api

data class GoogleUser(
    val token: String?,
    val email: String?,
    val name: String?,
) {
    sealed class Error(message: String? = null) : Throwable(message = message) {
        object NoCachedAccount : Error()
        class SignInError(message: String?) : Error(message)
    }
}