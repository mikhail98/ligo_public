package com.ligo.subfeature.createtrip

import com.ligo.common.BaseViewModel
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.model.DetailedDate
import com.ligo.data.model.Location
import com.ligo.data.model.Trip
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IPlaceSearchManager
import com.ligo.tools.api.SearchPlaceRequest
import com.ligo.tools.api.SearchPlaceResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class CreateTripFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val searchManager: IPlaceSearchManager,
    private val tripsCoordinator: IDriverTripCoordinator,
) : BaseViewModel(navigator, analytics) {

    var startPoint: Location? = null
        private set
    var endPoint: Location? = null
        private set

    private var buttonStateSubject: Subject<Boolean> =
        PublishSubject.create<Boolean>().toSerialized()

    private val clearAllFieldsSubject: Subject<Unit> =
        PublishSubject.create<Unit>().toSerialized()

    fun createTrip(date: DetailedDate?) {
        val startPoint = startPoint ?: return
        val endPoint = endPoint ?: return
        setLoading(true)

        tripsCoordinator.createTrip(startPoint, endPoint, date)
            .subscribeAndDispose(::handleTripCreated)
    }

    fun setPoint(location: Location?, origin: SearchPlaceRequest.Origin) {
        when (origin) {
            SearchPlaceRequest.Origin.START_TRIP_FROM -> startPoint = location
            SearchPlaceRequest.Origin.START_TRIP_TO -> endPoint = location
            else -> Unit
        }
        checkAllFieldsFilled()
    }

    fun checkAllFieldsFilled() {
        if (startPoint != null && endPoint != null) {
            buttonStateSubject.onNext(true)
        } else {
            buttonStateSubject.onNext(false)
        }
    }

    private fun handleTripCreated(trip: Trip) {
        clearAllFields()
        setLoading(false)
        navigator.open(Target.DriverTrip(trip._id))
    }

    private fun clearAllFields() {
        startPoint = null
        endPoint = null
        buttonStateSubject.onNext(false)
        clearAllFieldsSubject.onNext(Unit)
    }

    fun getButtonStateObservable(): Observable<Boolean> = buttonStateSubject

    fun getClearAllFieldsObservable(): Observable<Unit> = clearAllFieldsSubject

    fun getOnPlacePickedObservable(): Observable<SearchPlaceResult> =
        searchManager.getOnPlacePickedObservable()
}