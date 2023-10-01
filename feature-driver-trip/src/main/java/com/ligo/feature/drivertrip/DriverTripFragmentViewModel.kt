package com.ligo.feature.drivertrip

import com.google.android.gms.maps.model.LatLng
import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.common.map.MapUtil
import com.ligo.common.model.ParcelInTripUiModel
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.model.Location
import com.ligo.data.model.Parcel
import com.ligo.data.model.Trip
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.data.repo.parcel.IParcelRepo
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.ILocationTracker
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Optional

class DriverTripFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val appPreferences: IAppPreferences,
    private val parcelRepo: IParcelRepo,
    private val googleApisRepo: IGoogleApisRepo,
    private val locationTracker: ILocationTracker,
    private val chatsCoordinator: IChatsCoordinator,
    private val tripCoordinator: IDriverTripCoordinator,
) : BaseViewModel(navigator, analytics) {

    private val userId by lazy { appPreferences.getUser()?._id }

    private val routeSubject: Subject<List<LatLng>> =
        PublishSubject.create<List<LatLng>>().toSerialized()

    private val tripStartedSubject: Subject<Unit> =
        PublishSubject.create<Unit>().toSerialized()

    fun checkLocationService() {
        if (!locationTracker.isLocationServiceRunning()) {
            locationTracker.startLocationTracking()
        }
    }

    fun stopLocationService() {
        if (locationTracker.isLocationServiceRunning()) {
            locationTracker.stopLocationTracking()
        }
    }

    fun fetchAvailableParcel(parcelId: String?) {
        parcelId ?: return
        parcelRepo.getParcelById(parcelId)
            .subscribeAndDispose(tripCoordinator::setAvailableParcel)
    }

    fun acceptParcel(parcelId: String) {
        setLoading(true)
        tripCoordinator.acceptParcel(parcelId)
            .doOnComplete { chatsCoordinator.addOrUpdateChatByParcelId(parcelId) }
            .subscribeAndDispose {
                setLoading(false)
            }
    }

    fun declineParcel(parcelId: String) {
        tripCoordinator.declineParcel(parcelId).subscribeAndDispose {}
    }

    fun startTrip(tripId: String) {
        setLoading(true)
        tripCoordinator.startTrip(tripId)
            .subscribeAndDispose {
                setLoading(false)
                checkLocationService()
                tripStartedSubject.onNext(Unit)
            }
    }

    fun finishTrip(tripId: String) {
        setLoading(true)
        tripCoordinator.finishTrip(tripId)
            .subscribeAndDispose {
                setLoading(false)
                stopLocationService()
                navigator.close(Target.DriverTrip::class.java)
            }
    }

    fun fetchDirection(startPoint: Location, endPoint: Location) {
        googleApisRepo.searchForDirection(startPoint, endPoint)
            .map { MapUtil.decodeRoutePath(it.points) }
            .subscribeAndDispose(routeSubject::onNext)
    }

    fun getAvailableParcelObservable(): Observable<Parcel> =
        tripCoordinator.getAvailableParcelObservable()

    fun getOnRouteObservable(): Observable<List<LatLng>> = routeSubject

    fun getOnTripStartedObservable(): Observable<Unit> = tripStartedSubject

    fun getTripObservable(tripId: String): Observable<Pair<Optional<Trip>, Optional<List<ParcelInTripUiModel>>>> {
        return Observable.combineLatest(
            tripCoordinator.getTripListObservable(),
            chatsCoordinator.getChatsObservable()
        ) { trips, chats ->
            val trip = trips.find { trip -> trip._id == tripId }
            val parcelInTripList = trip?.parcelList?.map { parcel ->
                val messages = chats.find { chat -> chat.parcel._id == parcel._id }?.messages
                val unreadMessageCount =
                    messages?.filter { !it.isRead && it.userId != userId }?.size ?: 0

                ParcelInTripUiModel.fromParcel(unreadMessageCount, parcel)
            }
                ?.sortedWith(
                    compareByDescending<ParcelInTripUiModel> { it.priority }
                        .thenByDescending { it.createdAt }
                )

            Optional.ofNullable(trip) to Optional.ofNullable(parcelInTripList)
        }
    }
}
