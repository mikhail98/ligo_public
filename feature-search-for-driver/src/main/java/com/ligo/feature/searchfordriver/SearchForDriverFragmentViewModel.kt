package com.ligo.feature.searchfordriver

import com.google.android.gms.maps.model.LatLng
import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.common.map.MapUtil
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.model.Location
import com.ligo.data.model.Parcel
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.data.socket.ISocketService
import com.ligo.data.socket.event.IncomingSocketEvent
import com.ligo.google.api.IAnalytics
import com.ligo.google.api.IRemoteConfig
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Optional

class SearchForDriverFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val remoteConfig: IRemoteConfig,
    private val socketService: ISocketService,
    private val googleApisRepo: IGoogleApisRepo,
    private val chatsCoordinator: IChatsCoordinator,
    private val parcelsCoordinator: ISenderParcelsCoordinator,
) : BaseViewModel(navigator, analytics) {

    private var parcelId: String? = null

    private val routeSubject: Subject<List<LatLng>> =
        PublishSubject.create<List<LatLng>>().toSerialized()

    private val showNotEnoughDriversDialogSubject: Subject<Unit> =
        PublishSubject.create<Unit>().toSerialized()

    override fun onCreate() {
        if (remoteConfig.getBoolean(IRemoteConfig.SHOW_NOT_ENOUGH_DRIVERS)) {
            showNotEnoughDriversDialogSubject.onNext(Unit)
        }

        socketService.getOnIncomingEventObservable()
            .subscribeAndDispose(::handleSocketEvent)
    }

    private fun handleSocketEvent(event: IncomingSocketEvent) {
        when (event) {
            is IncomingSocketEvent.ParcelAccepted -> {
                chatsCoordinator.addOrUpdateChatByParcelId(event.parcel._id)
                navigator.close(Target.SearchForDriver::class.java)
                navigator.open(Target.SenderParcel(event.parcel._id))
            }

            else -> Unit
        }
    }

    fun fetchDirection(startPoint: Location, endPoint: Location) {
        googleApisRepo.searchForDirection(startPoint, endPoint)
            .map { MapUtil.decodeRoutePath(it.points) }
            .subscribeAndDispose(routeSubject::onNext)
    }

    fun cancelParcel() {
        val parcelId = parcelId ?: return
        setLoading(true)
        parcelsCoordinator.cancelParcel(parcelId)
            .subscribeAndDispose {
                navigator.close(Target.SearchForDriver::class.java)
            }
    }

    fun getOnRouteObservable(): Observable<List<LatLng>> = routeSubject

    fun getOnShowEnoughDriverDialogObservable(): Observable<Unit> =
        showNotEnoughDriversDialogSubject

    fun getParcelObservable(parcelId: String): Observable<Optional<Parcel>> {
        this.parcelId = parcelId
        return parcelsCoordinator.getParcelListObservable()
            .map { Optional.ofNullable(it.find { parcel -> parcel._id == parcelId }) }
    }
}