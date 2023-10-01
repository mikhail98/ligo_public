package com.ligo.feature.auth

import com.ligo.common.BaseViewModel
import com.ligo.core.Initializable
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.LoginUser
import com.ligo.data.model.User
import com.ligo.data.model.UserRequest
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.auth.IAuthRepo
import com.ligo.data.repo.user.IUserRepo
import com.ligo.google.api.GoogleUser
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IAuthManager
import com.ligo.google.api.IFcmTokenManager
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IInitializer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class AuthFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val userRepo: IUserRepo,
    private val authRepo: IAuthRepo,
    private val authManager: IAuthManager,
    private val initializer: IInitializer,
    private val appPreferences: IAppPreferences,
    private val fcmTokenManager: IFcmTokenManager,
) : BaseViewModel(navigator, analytics) {

    override fun onCreate() {
        authManager.getGoogleAuthUserObservable()
            .subscribeAndDispose(::handleGoogleAuthUser)
    }

    override fun handleOtherErrors(throwable: Throwable) {
        when (throwable) {
            is GoogleUser.Error.SignInError -> {
                handleSnackbar(ConfigStringKey.ERROR_WHILE_GOOGLE_AUTHORIZATION)
            }
        }
    }

    private fun handleGoogleAuthUser(data: Pair<GoogleUser?, Throwable?>) {
        val user = data.first
        val throwable = data.second
        if (user != null) {
            handleGoogleAuthUser(user)
        }
        if (throwable != null) {
            setLoading(false)
            handleError(throwable)
        }
    }

    private fun handleGoogleAuthUser(user: GoogleUser) {
        userRepo.checkUserExists(user.email.orEmpty())
            .map { user to it }
            .flatMap(::handleUserExists)
            .subscribeAndDispose(::handleGoogleSignInResult)
    }

    private fun handleUserExists(data: Pair<GoogleUser, Boolean>): Single<GoogleSignInResult> {
        return if (data.second) {
            getSignInSingle(data.first)
        } else {
            getSignUpSingle(data.first)
        }
    }

    private fun getSignInSingle(googleUser: GoogleUser): Single<GoogleSignInResult> {
        val loginUser = LoginUser(googleUser.email.orEmpty(), googleUser.token.orEmpty())
        return authRepo.login(loginUser)
            .flatMap(::handleUserLogged)
            .map { GoogleSignInResult(GoogleSignInResult.Type.SIGN_IN, googleUser) }
    }

    private fun handleGoogleSignInResult(result: GoogleSignInResult) {
        setLoading(false)
        when (result.type) {
            GoogleSignInResult.Type.SIGN_IN -> navigator.open(Target.Home)
            GoogleSignInResult.Type.SIGN_UP -> proceedToRegistration(result.user)
        }
    }

    private fun getSignUpSingle(user: GoogleUser): Single<GoogleSignInResult> {
        return Single.just(GoogleSignInResult(GoogleSignInResult.Type.SIGN_UP, user))
    }

    private fun handleUserLogged(user: User): Single<User> {
        appPreferences.saveUser(user)
        initializer.on(Initializable.On.LOGIN)
        return fcmTokenManager.fetchToken()
            .flatMap { token ->
                userRepo.updateFcmToken(user._id, token)
                    .subscribeOn(Schedulers.io())
                    .andThen(Single.just(user))
            }
    }

    private fun proceedToRegistration(googleUser: GoogleUser) {
        setLoading(false)

        val registerUser = UserRequest(
            googleToken = googleUser.token,
            name = googleUser.name.orEmpty(),
            email = googleUser.email.orEmpty()
        )

        appPreferences.saveRegisterUser(registerUser)
        navigator.open(Target.SetupPhone)
    }

    fun signOutGoogle() {
        authManager.signOut()
    }

    fun signInGoogle() {
        setLoading(true)
        authManager.signIn()
    }

    class GoogleSignInResult(
        val type: Type,
        val user: GoogleUser,
    ) {
        enum class Type {
            SIGN_IN, SIGN_UP
        }
    }
}