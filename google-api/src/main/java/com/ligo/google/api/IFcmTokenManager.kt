package com.ligo.google.api

import io.reactivex.rxjava3.core.Single

interface IFcmTokenManager {

    fun fetchToken(listener: (String?) -> Unit)

    fun fetchToken(): Single<String>

    object CannotFetchFcmToken : Throwable()
}