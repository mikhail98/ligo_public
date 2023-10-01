package com.ligo.google.api

import android.app.Activity
import android.content.Intent
import io.reactivex.rxjava3.core.Observable

interface IAuthManager {

    fun createSignInClient(activity: Activity)

    fun signIn()

    fun signOut()

    fun fetchSignedUser()

    fun handleGoogleAuth(requestCode: Int, data: Intent?)

    fun getGoogleAuthUserObservable(): Observable<Pair<GoogleUser?, Throwable?>>
}