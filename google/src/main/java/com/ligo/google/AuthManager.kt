package com.ligo.google

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.ligo.google.api.GoogleUser
import com.ligo.google.api.IAuthManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class AuthManager : IAuthManager {

    companion object {
        const val REQUEST_CODE = 1349
        private val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("530511537125-qg2t6njv6p92c91d4jvsoutihtg73o2n.apps.googleusercontent.com")
            .requestEmail().build()
    }

    private val googleAuthUser: Subject<Pair<GoogleUser?, Throwable?>> =
        PublishSubject.create<Pair<GoogleUser?, Throwable?>>().toSerialized()

    private var appActivity: Activity? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var account: GoogleUser? = null

    override fun createSignInClient(activity: Activity) {
        if (googleSignInClient != null && appActivity != null) return
        appActivity = activity
        googleSignInClient = GoogleSignIn.getClient(activity, options)

        account = GoogleSignIn.getLastSignedInAccount(activity)?.let(::mapAccountToGoogleUser)
    }

    override fun fetchSignedUser() {
        if (account != null) {
            googleAuthUser.onNext(account to null)
        } else {
            googleAuthUser.onNext(null to GoogleUser.Error.NoCachedAccount)
        }
    }

    override fun signIn() {
        if (account == null) {
            googleSignInClient?.signInIntent?.apply {
                appActivity?.startActivityForResult(this, REQUEST_CODE)
            }
        } else {
            googleAuthUser.onNext(account to null)
        }
    }

    override fun signOut() {
        googleSignInClient?.signOut()

        account = null
    }

    override fun handleGoogleAuth(requestCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE) return

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            account = mapAccountToGoogleUser(task.result)
            googleAuthUser.onNext(account to null)
        } catch (e: Throwable) {
            googleAuthUser.onNext(null to GoogleUser.Error.SignInError(e.message))
        }
    }

    private fun mapAccountToGoogleUser(account: GoogleSignInAccount): GoogleUser {
        return GoogleUser(account.idToken, account.email, account.displayName)
    }

    override fun getGoogleAuthUserObservable(): Observable<Pair<GoogleUser?, Throwable?>> =
        googleAuthUser
}