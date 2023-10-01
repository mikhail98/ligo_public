package com.ligo.feature.searchplaceonmap

import com.ligo.common.BaseViewModel
import com.ligo.core.printError
import com.ligo.data.model.Location
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.ILocationManager
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IPlaceSearchManager
import com.ligo.tools.api.SearchPlaceRequest
import com.ligo.tools.api.SearchPlaceResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class SearchPlaceOnMapFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val appPreferences: IAppPreferences,
    private val locationManager: ILocationManager,
    private val searchManager: IPlaceSearchManager,
) : BaseViewModel(navigator, analytics) {

    private val placeInfoSubject: Subject<Optional<Location>> =
        PublishSubject.create<Optional<Location>>().toSerialized()

    private var currentLocation: Location? = null
    private var fetchPlaceDisposable: Disposable? = null

    fun fetchPlaceInfo(latitude: Double, longitude: Double) {
        fetchPlaceDisposable?.dispose()
        locationManager.fetchLocationInfo(latitude, longitude)
            .doOnSuccess {
                currentLocation = it.getOrNull()
            }
            .subscribe(placeInfoSubject::onNext, ::printError)
            .also { fetchPlaceDisposable = it }
    }

    fun onPlaceSelected(origin: SearchPlaceRequest.Origin) {
        val location = currentLocation ?: return
        searchManager.pickPlace(SearchPlaceResult(location, origin))
        appPreferences.addRecentLocationSearch(location)
        navigator.close(Target.SearchPlace::class.java)
        navigator.close(Target.SearchPlaceOnMap::class.java)
    }

    fun getOnPlaceInfoObservable(): Observable<Optional<Location>> = placeInfoSubject
}