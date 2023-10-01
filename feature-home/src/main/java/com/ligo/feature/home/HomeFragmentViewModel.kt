package com.ligo.feature.home

import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.core.printError
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.model.TripStatus
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.OnboardingType
import com.ligo.navigator.api.Target
import com.ligo.toggler.api.Feature
import com.ligo.toggler.api.IToggler
import com.ligo.tools.api.ILocationTracker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class HomeFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val appPreferences: IAppPreferences,
    private val locationTracker: ILocationTracker,
    private val chatsCoordinator: IChatsCoordinator,
    private val tripsCoordinator: IDriverTripCoordinator,
    private val toggler: IToggler,
) : BaseViewModel(navigator, analytics) {

    private var tripListDisposable: Disposable? = null

    fun requestNextStep(chatId: String?) {
        val onboardingType = OnboardingType.MAIN
        when {
            !appPreferences.isOnboardingShown(onboardingType.typeKey) -> {
                appPreferences.setOnboardingShown(onboardingType.typeKey)
                navigator.open(Target.Onboarding(onboardingType))
            }

            !chatId.isNullOrEmpty() -> {
                navigator.open(Target.Chat(chatId))
            }

            else -> {
                tripListDisposable = tripsCoordinator.getTripListObservable()
                    .subscribeOn(Schedulers.io())
                    .subscribe({ trips ->
                        val trip = trips.find { it.status == TripStatus.ACTIVE }
                        if (trip != null) {
                            if (!locationTracker.isLocationServiceRunning()) {
                                locationTracker.startLocationTracking()
                            }
                            if (appPreferences.getOpenTripState()) {
                                navigator.open(Target.DriverTrip(trip._id))
                            }
                        }
                        tripListDisposable?.dispose()
                    }, ::printError)
            }
        }
    }

    fun getUnreadChatCountObservable(): Observable<Int> =
        Observable.combineLatest(
            chatsCoordinator.getUnreadChatCountObservable(),
            toggler.getAvailableFeatureListObservable()
                .map { it.contains(Feature.CHAT) }
        ) { unreadCount, chatAvailable ->
            if (chatAvailable) unreadCount else 0
        }
}