package com.ligo.feature.parcelintrip

import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.common.BaseViewModel
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.model.Parcel
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.user.IUserRepo
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.toggler.api.Feature
import com.ligo.toggler.api.IToggler
import com.ligo.tools.api.IQrManager
import com.ligo.tools.api.ScanQR
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.Optional

class ParcelInTripFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val toggler: IToggler,
    private val userRepo: IUserRepo,
    private val qrManager: IQrManager,
    private val appPreferences: IAppPreferences,
    private val chatsCoordinator: IChatsCoordinator,
    private val tripsCoordinator: IDriverTripCoordinator,
) : BaseViewModel(navigator, analytics) {

    private var parcelId: String? = null

    private val cantPickParcelSubject: Subject<Unit> =
        PublishSubject.create<Unit>().toSerialized()

    private val cantDeliverParcelSubject: Subject<Unit> =
        PublishSubject.create<Unit>().toSerialized()

    private val parcelQRScanned: Subject<ScanQR.Origin> =
        PublishSubject.create<ScanQR.Origin>().toSerialized()

    private val showConfirmPickupDialogSubject: Subject<String> =
        PublishSubject.create<String>().toSerialized()

    override fun onCreate() {
        getOnParcelScanDataObservable()
            .subscribeAndDispose(::handleParcelScanData)
    }

    private fun handleParcelScanData(data: ParcelScanData) {
        when (data.type) {
            ParcelScanData.Type.DELIVERY -> onDeliverQrScanned(data.parcelId, data.secret)
            ParcelScanData.Type.PICKUP -> requestParcelPickup(data.parcelId)
        }
    }

    private fun onDeliverQrScanned(parcelId: String, secret: String?) {
        setLoading(true)
        if (parcelId.isEmpty() || secret.isNullOrEmpty() || this.parcelId != parcelId) {
            cantDeliverParcelSubject.onNext(Unit)
        } else {
            tripsCoordinator.deliverParcel(parcelId, secret)
                .subscribeAndDispose { onParcelQRFinallyScanned(ScanQR.Origin.DELIVERY) }
        }
    }

    private fun requestParcelPickup(parcelId: String) {
        if (parcelId.isEmpty() || this.parcelId != parcelId) {
            cantPickParcelSubject.onNext(Unit)
            return
        }
        showConfirmPickupDialogSubject.onNext(parcelId)
    }

    fun pickupParcel(parcelId: String) {
        tripsCoordinator.pickupParcel(parcelId)
            .subscribeAndDispose { onParcelQRFinallyScanned(ScanQR.Origin.PICK) }
    }

    fun updateSenderRating(userId: String, rating: Int) {
        userRepo.updateUserRating(userId, rating)
            .subscribeAndDispose {}
    }

    fun scanQr(origin: ScanQR.Origin) {
        qrManager.scanQr(origin)
    }

    private fun onParcelQRFinallyScanned(origin: ScanQR.Origin) {
        setLoading(false)
        parcelQRScanned.onNext(origin)
    }

    private fun getOnParcelScanDataObservable(): Observable<ParcelScanData> =
        qrManager.getOnScannedQRObservable()
            .map {
                when (it.origin) {
                    ScanQR.Origin.PICK ->
                        ParcelScanData(ParcelScanData.Type.PICKUP, it.data, null)

                    ScanQR.Origin.DELIVERY -> {
                        val dataArray = it.data.split('/')
                        val parcelId = dataArray.getOrNull(0).orEmpty()
                        val secret = dataArray.getOrNull(1).orEmpty()
                        ParcelScanData(ParcelScanData.Type.DELIVERY, parcelId, secret)
                    }
                }
            }

    fun getChatAvailableObservable(): Observable<Boolean> =
        toggler.getAvailableFeatureListObservable()
            .map { it.contains(Feature.CHAT) }

    fun openChatForParcel(parcelId: String) {
        val chat = chatsCoordinator.getChatForParcel(parcelId) ?: return
        navigator.open(Target.Chat(chat._id))
    }

    fun getParcelQRScannedObservable(): Observable<ScanQR.Origin> =
        parcelQRScanned

    fun getOnCantPickParcelObservable(): Observable<Unit> =
        cantPickParcelSubject

    fun getOnCantDeliverParcelObservable(): Observable<Unit> =
        cantDeliverParcelSubject

    fun getOnShowConfirmPickupDialogObservable(): Observable<String> =
        showConfirmPickupDialogSubject

    fun getUnreadMessagesCountObservable(parcelId: String): Observable<Int> {
        val userId = appPreferences.getUser()?._id
        return Observable.combineLatest(
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
    }

    fun getParcelObservable(parcelId: String): Observable<Optional<Parcel>> {
        this.parcelId = parcelId
        return tripsCoordinator.getTripListObservable()
            .map { tripList ->
                val parcel = tripList.flatMap { it.parcelList }.find { it._id == parcelId }
                Optional.ofNullable(parcel)
            }
    }
}