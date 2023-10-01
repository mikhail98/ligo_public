package com.ligo.feature.aboutapp

import com.ligo.common.BaseViewModel
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class AboutAppFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val appPreferences: IAppPreferences,
) : BaseViewModel(navigator, analytics) {

    private val openTripStateSubject: Subject<Boolean> =
        PublishSubject.create<Boolean>().toSerialized()

    fun fetchOpenTripState() {
        openTripStateSubject.onNext(appPreferences.getOpenTripState())
    }

    fun saveOpenTripState(newState: Boolean) {
        appPreferences.saveOpenTripState(newState)
    }

    fun getOpenTripStateObservable(): Observable<Boolean> =
        openTripStateSubject
}