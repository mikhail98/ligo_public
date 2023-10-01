package com.ligo.feature.selectrole

import com.ligo.common.BaseViewModel
import com.ligo.core.Initializable
import com.ligo.data.model.User
import com.ligo.data.model.UserRole
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.user.IUserRepo
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IFcmTokenManager
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IInitializer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class SelectRoleFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val fcmTokenManager: IFcmTokenManager,
    private val appPreferences: IAppPreferences,
    private val userRepo: IUserRepo,
    private val initializer: IInitializer,
) : BaseViewModel(navigator, analytics) {

    private var userRole = UserRole.SENDER

    private val userRoleSubject: Subject<UserRole> =
        PublishSubject.create<UserRole>().toSerialized()

    fun setUserRole(userRole: UserRole) {
        this.userRole = userRole
        userRoleSubject.onNext(userRole)
    }

    fun register() {
        val user = appPreferences.getRegisterUser()?.copy(role = userRole) ?: return

        setLoading(true)

        fcmTokenManager.fetchToken()
            .map { user.copy(fcmToken = it) }
            .flatMap(userRepo::createUser)
            .subscribeAndDispose(::handleUser)
    }

    private fun handleUser(user: User) {
        setLoading(false)
        appPreferences.saveUser(user)
        initializer.on(Initializable.On.LOGIN)
        appPreferences.saveRegisterUser(null)
        navigator.close(Target.SetupPhone::class.java)
        navigator.open(Target.Home)
    }

    fun getUserRoleObservable(): Observable<UserRole> = userRoleSubject
}