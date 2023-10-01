package com.ligo.feature.splash

import com.ligo.common.BaseViewModel
import com.ligo.core.BuildConfig
import com.ligo.core.Initializable
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.user.IUserRepo
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IRemoteConfig
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IInitializer
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class SplashFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val userRepo: IUserRepo,
    private val initializer: IInitializer,
    private val remoteConfig: IRemoteConfig,
    private val appPreferences: IAppPreferences,
    private val configManager: ILocalizationManager,
) : BaseViewModel(navigator, analytics) {

    private val updateAppSubject: Subject<Pair<Boolean, Boolean>> =
        PublishSubject.create<Pair<Boolean, Boolean>>().toSerialized()

    fun init() {
        configManager.updateLocalization().subscribeAndDispose { onLocalizationUpdated() }
    }

    private fun onLocalizationUpdated() {
        if (fetchAppUpdate()) {
            return
        }
        val oldUser = appPreferences.getUser()
        if (oldUser != null) {
            initializer.on(Initializable.On.LOGIN)
            userRepo.getUserById(oldUser._id)
                .map { appPreferences.saveUser(it.copy(authToken = oldUser.authToken)) }
                .subscribeAndDispose {
                    navigator.open(Target.Home)
                }
        } else {
            navigator.open(Target.Auth)
        }
    }

    private fun fetchAppUpdate(): Boolean {
        val currentVersion = BuildConfig.VERSION_CODE
        val updateAppRecommendationVersion =
            remoteConfig.getLong(IRemoteConfig.UPDATE_APP_RECOMMENDATION_VERSION)
        val updateAppMandatoryVersion =
            remoteConfig.getLong(IRemoteConfig.UPDATE_APP_MANDATORY_VERSION)
        updateAppSubject.onNext((currentVersion < updateAppRecommendationVersion) to (currentVersion < updateAppMandatoryVersion))
        return currentVersion < updateAppMandatoryVersion
    }

    fun getOnUpdateAppObservable(): Observable<Pair<Boolean, Boolean>> =
        updateAppSubject

    fun logout() {
        appPreferences.logout()
    }
}