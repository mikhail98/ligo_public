package com.ligo.google

import com.google.firebase.messaging.FirebaseMessaging
import com.ligo.core.printError
import com.ligo.google.api.IFcmTokenManager
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

internal class FcmTokenManager(
    private val firebaseMessaging: FirebaseMessaging,
) : IFcmTokenManager {

    override fun fetchToken(listener: (String?) -> Unit) {
        firebaseMessaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                listener.invoke(null)
            } else {
                listener.invoke(task.result.orEmpty())
            }
        }.addOnFailureListener {
            printError(it)
            listener.invoke(null)
        }
    }

    override fun fetchToken(): Single<String> {
        return Single.create { emitter ->
            fetchToken { token ->
                if (token != null) {
                    emitter.onSuccess(token)
                } else {
                    emitter.onError(IFcmTokenManager.CannotFetchFcmToken)
                }
            }
        }.observeOn(Schedulers.io())
    }
}