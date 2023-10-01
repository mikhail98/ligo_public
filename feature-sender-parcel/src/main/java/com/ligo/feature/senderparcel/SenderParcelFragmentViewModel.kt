package com.ligo.feature.senderparcel

import com.google.android.gms.maps.model.LatLng
import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.common.map.MapUtil
import com.ligo.core.BuildConfig
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.model.Location
import com.ligo.data.model.LocationUpdate
import com.ligo.data.model.Parcel
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.data.repo.parcel.IParcelRepo
import com.ligo.data.repo.user.IUserRepo
import com.ligo.data.socket.ISocketService
import com.ligo.data.socket.event.IncomingSocketEvent
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.toggler.api.Feature
import com.ligo.toggler.api.IToggler
import com.ligo.tools.api.IQrManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Optional

class SenderParcelFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    appPreferences: IAppPreferences,
    private val toggler: IToggler,
    private val userRepo: IUserRepo,
    private val qrManager: IQrManager,
    private val parcelRepo: IParcelRepo,
    private val socketService: ISocketService,
    private val googleApisRepo: IGoogleApisRepo,
    private val chatsCoordinator: IChatsCoordinator,
    private val parcelsCoordinator: ISenderParcelsCoordinator,
) : BaseViewModel(navigator, analytics) {

    companion object {
        private val BASE_DELIVERY_LINK = if (BuildConfig.SANDBOX) {
            "https://mikyar2.dreamhosters.com/parcel/?"
        } else {
            "https://withligo.com/parcel/?"
        }
    }

    private val routeSubject: Subject<List<LatLng>> =
        BehaviorSubject.create<List<LatLng>>().toSerialized()

    private val deliveryLinkSubject: Subject<String> =
        PublishSubject.create<String>().toSerialized()

    private val locationUpdateSubject: Subject<LocationUpdate> =
        PublishSubject.create<LocationUpdate>().toSerialized()

    private val userId = appPreferences.getUser()?._id

    override fun onCreate() {
        socketService.getOnIncomingEventObservable()
            .subscribeAndDispose(::handleSocketEvent)
    }

    private fun handleSocketEvent(event: IncomingSocketEvent) {
        when (event) {
            is IncomingSocketEvent.DriverLocationUpdated -> {
                locationUpdateSubject.onNext(event.locationUpdate)
            }

            else -> Unit
        }
    }

    fun fetchDirection(startPoint: Location, endPoint: Location) {
        googleApisRepo.searchForDirection(startPoint, endPoint)
            .map { MapUtil.decodeRoutePath(it.points) }
            .subscribeAndDispose(routeSubject::onNext)
    }

    fun generateDeliveryLink(parcelId: String) {
        setLoading(true)
        parcelRepo.getSecret(parcelId)
            .subscribeAndDispose { secret ->
                setLoading(false)
                deliveryLinkSubject.onNext(BASE_DELIVERY_LINK + parcelId + '/' + secret.secret)
            }
    }

    fun updateUserRating(userId: String, rating: Int) {
        userRepo.updateUserRating(userId, rating)
            .subscribeAndDispose {}
    }

    fun getOnRouteObservable(): Observable<List<LatLng>> = routeSubject

    fun getOnDeliveryLinkObservable(): Observable<String> = deliveryLinkSubject

    fun getOnLocationUpdateObservable(): Observable<LocationUpdate> = locationUpdateSubject

    fun getChatAvailableObservable(): Observable<Boolean> =
        toggler.getAvailableFeatureListObservable()
            .map { it.contains(Feature.CHAT) }

    fun openChatForParcel(parcelId: String) {
        val chat = chatsCoordinator.getChatForParcel(parcelId) ?: return
        navigator.open(Target.Chat(chat._id))
    }

    fun getUnreadMessagesCountObservable(parcelId: String): Observable<Int> =
        Observable.combineLatest(
            chatsCoordinator.getChatsObservable()
                .map { chats ->
                    chats.find { chat -> chat.parcel._id == parcelId }
                        ?.messages
                        ?.filter { message -> message.userId != userId && !message.isRead }
                        ?.size ?: 0
                },
            getChatAvailableObservable()
        ) { unreadCount, chatAvailable ->
            if (chatAvailable) unreadCount else 0
        }

    fun getParcelObservable(parcelId: String): Observable<Optional<Parcel>> {
        return parcelsCoordinator.getParcelListObservable()
            .map { Optional.ofNullable(it.find { parcel -> parcel._id == parcelId }) }
    }

    fun showQr(data: String) {
        qrManager.showQr(data)
    }

    fun getOnParcelDeliveredSubject(): Observable<Parcel> =
        parcelsCoordinator.getOnParcelDeliveredObservable()
}