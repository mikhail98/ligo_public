package com.ligo.data.coordinator.parcels

import com.ligo.core.Initializable
import com.ligo.core.printError
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRequest
import com.ligo.data.model.ParcelStatus
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.parcel.IParcelRepo
import com.ligo.data.repo.user.IUserRepo
import com.ligo.data.socket.ISocketService
import com.ligo.data.socket.event.IncomingSocketEvent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class SenderParcelsCoordinator(
    private val userRepo: IUserRepo,
    private val parcelRepo: IParcelRepo,
    private val socketService: ISocketService,
    private val appPreferences: IAppPreferences,
) : ISenderParcelsCoordinator {

    private val parcelListSubject: Subject<List<Parcel>> =
        BehaviorSubject.create<List<Parcel>>().toSerialized()

    private val parcelDeliveredSubject: Subject<Parcel> =
        PublishSubject.create<Parcel>().toSerialized()

    override val initOnList: List<Initializable.On> = listOf(Initializable.On.LOGIN)
    override val connectOnList: List<Initializable.On> = listOf(Initializable.On.NEVER)
    override val clearOnList: List<Initializable.On> = listOf(Initializable.On.LOGOUT)

    private val parcelMap: MutableMap<String, Parcel> = mutableMapOf()
    private var userId: String? = null

    private var refreshParcelsDisposable: Disposable? = null
    private var socketDisposable: Disposable? = null

    override fun init() {
        this.userId = appPreferences.getUser()?._id
        refreshParcels()

        socketDisposable?.dispose()
        socketService.getOnIncomingEventObservable()
            .subscribe(::handleEvents, ::printError)
            .also { socketDisposable = it }
    }

    private fun handleEvents(event: IncomingSocketEvent) {
        when (event) {
            is IncomingSocketEvent.ParcelAccepted -> {
                parcelMap[event.parcel._id] = event.parcel
                triggerRefreshParcels()
            }

            is IncomingSocketEvent.ParcelRejected -> {
                parcelMap[event.parcel._id] = event.parcel
                triggerRefreshParcels()
            }

            is IncomingSocketEvent.ParcelPicked -> {
                parcelMap[event.parcel._id] = event.parcel
                triggerRefreshParcels()
            }

            is IncomingSocketEvent.ParcelDelivered -> {
                parcelDeliveredSubject.onNext(event.parcel)
                parcelMap[event.parcel._id] = event.parcel
                triggerRefreshParcels()
            }

            else -> Unit
        }
    }

    override fun clear() {
        refreshParcelsDisposable?.dispose()
        socketDisposable?.dispose()
        parcelMap.clear()
        triggerRefreshParcels()
    }

    override fun createParcel(parcel: ParcelRequest): Single<Parcel> {
        return parcelRepo.createParcel(parcel)
            .doOnSuccess {
                parcelMap[it._id] = it
                triggerRefreshParcels()
            }
    }

    override fun cancelParcel(parcelId: String): Completable {
        return parcelRepo.cancelParcel(parcelId)
            .doOnSuccess {
                parcelMap[it._id]?.status = ParcelStatus.CANCELLED
                triggerRefreshParcels()
            }
            .flatMapCompletable { Completable.complete() }
    }

    override fun refreshParcels() {
        val userId = userId ?: return
        userRepo.getSenderParcels(userId)
            .subscribeOn(Schedulers.io())
            .subscribe({
                parcelMap.clear()
                it.forEach { parcel -> parcelMap[parcel._id] = parcel }
                triggerRefreshParcels()
            }, ::printError)
            .also { refreshParcelsDisposable = it }
    }

    private fun triggerRefreshParcels() {
        parcelListSubject.onNext(parcelMap.values.toList())
    }

    override fun getParcelListObservable(): Observable<List<Parcel>> =
        parcelListSubject

    override fun getOnParcelDeliveredObservable(): Observable<Parcel> =
        parcelDeliveredSubject
}