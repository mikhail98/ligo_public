package com.ligo.feature.searchplace

import com.ligo.common.BaseViewModel
import com.ligo.core.printError
import com.ligo.data.model.Location
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.feature.searchplace.adapter.SearchItem
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.ILocationManager
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IPlaceSearchManager
import com.ligo.tools.api.SearchPlaceRequest
import com.ligo.tools.api.SearchPlaceResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlin.jvm.optionals.getOrNull

class SearchPlaceFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val appPreferences: IAppPreferences,
    private val googleMapsRepo: IGoogleApisRepo,
    private val locationManager: ILocationManager,
    private val searchManager: IPlaceSearchManager,
) : BaseViewModel(navigator, analytics) {

    private val searchPlaceCompositeDisposable = CompositeDisposable()

    private val searchItemSubject: Subject<List<SearchItem>> =
        PublishSubject.create<List<SearchItem>>().toSerialized()

    private var fetchPlaceDisposable: Disposable? = null

    fun search(query: String) {
        if (query.isEmpty()) {
            val list = ArrayList(appPreferences.getRecentLocationSearch()).map {
                SearchItem(SearchItem.Type.RESULT, it)
            }.toMutableList()

            list.add(0, SearchItem(SearchItem.Type.SEARCH_ON_MAP, null))
            list.add(1, SearchItem(SearchItem.Type.RECENT, null))
            searchItemSubject.onNext(list)
        } else {
            searchPlaceCompositeDisposable.clear()
            setLoading(true)
            googleMapsRepo.searchForResults(query)
                .map {
                    it.results.map { result ->
                        val lat = result.geometry.location.lat
                        val lng = result.geometry.location.lng
                        val location = Location(lat, lng, result.name, result.address, result.name)
                        SearchItem(SearchItem.Type.RESULT, location, result.iconUrl)
                    }.toMutableList()
                }
                .subscribeAndDispose(searchPlaceCompositeDisposable) { result ->
                    result.add(0, SearchItem(SearchItem.Type.SEARCH_ON_MAP, null))
                    searchItemSubject.onNext(result)
                    setLoading(false)
                }
        }
    }

    fun onLocationSelected(location: Location?, origin: SearchPlaceRequest.Origin) {
        if (location == null) return
        fetchPlaceDisposable?.dispose()

        locationManager.fetchLocationInfo(location.latitude, location.longitude)
            .flatMap {
                val data = it.getOrNull()
                if (data != null) {
                    Single.just(data)
                } else {
                    Single.error(Throwable())
                }
            }
            .subscribe({
                val selectedLocation = location.copy(cityName = it.cityName)
                searchManager.pickPlace(SearchPlaceResult(selectedLocation, origin))
                appPreferences.addRecentLocationSearch(selectedLocation)
                navigator.close(Target.SearchPlace::class.java)
            }, ::printError)
            .also { fetchPlaceDisposable = it }
    }

    override fun onCleared() {
        super.onCleared()
        searchPlaceCompositeDisposable.clear()
    }

    fun getOnSearchItemsObservable(): Observable<List<SearchItem>> = searchItemSubject
}