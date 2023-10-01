package com.ligo.data.coordinator.trips

import com.ligo.core.Initializable
import com.ligo.core.printError
import com.ligo.data.model.DetailedDate
import com.ligo.data.model.Location
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRejectRequest
import com.ligo.data.model.ParcelStatus
import com.ligo.data.model.Trip
import com.ligo.data.model.TripStatus
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.parcel.IParcelRepo
import com.ligo.data.repo.trip.ITripRepo
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

internal class DriverTripCoordinator(
    private val tripRepo: ITripRepo,
    private val userRepo: IUserRepo,
    private val parcelRepo: IParcelRepo,
    private val socketService: ISocketService,
    private val appPreferences: IAppPreferences,
) : IDriverTripCoordinator {

    private val tripListSubject: Subject<List<Trip>> =
        BehaviorSubject.create<List<Trip>>().toSerialized()

    private val availableParcelSubject: Subject<Parcel> =
        PublishSubject.create<Parcel>().toSerialized()

    private val availableParcelCancelledSubject: Subject<Parcel> =
        PublishSubject.create<Parcel>().toSerialized()

    override val initOnList: List<Initializable.On> = listOf(Initializable.On.LOGIN)
    override val connectOnList: List<Initializable.On> = listOf(Initializable.On.NEVER)
    override val clearOnList: List<Initializable.On> = listOf(Initializable.On.LOGOUT)

    private val tripMap: MutableMap<String, Trip> = mutableMapOf()
    private var userId: String? = null

    private var refreshTripsDisposable: Disposable? = null
    private var socketDisposable: Disposable? = null

    override fun init() {
        userId = appPreferences.getUser()?._id
        refreshTrips()

        socketDisposable?.dispose()
        socketService.getOnIncomingEventObservable()
            .subscribe({ event ->
                when (event) {
                    is IncomingSocketEvent.ParcelAvailable -> setAvailableParcel(event.parcel)
                    is IncomingSocketEvent.ParcelCancelled -> {
                        availableParcelCancelledSubject.onNext(event.parcel)
                    }

                    else -> Unit
                }
            }, ::printError)
            .also { socketDisposable = it }
    }

    override fun clear() {
        refreshTripsDisposable?.dispose()
        socketDisposable?.dispose()
        tripMap.clear()
        triggerRefreshTrips()
    }

    override fun startTrip(tripId: String): Single<String> {
        return tripRepo.startTrip(tripId)
            .observeOn(Schedulers.io())
            .doOnComplete {
                tripMap[tripId]?.status = TripStatus.ACTIVE
                triggerRefreshTrips()
            }
            .toSingle { tripId }
    }

    override fun finishTrip(tripId: String): Single<String> {
        return tripRepo.finishTrip(tripId)
            .observeOn(Schedulers.io())
            .doOnComplete {
                tripMap[tripId]?.status = TripStatus.FINISHED
                triggerRefreshTrips()
            }
            .toSingle { tripId }
    }

    override fun refreshTrips() {
        val userId = userId ?: return
        userRepo.getDriverTrips(userId)
            .subscribeOn(Schedulers.io())
            .subscribe({
                tripMap.clear()
                it.forEach { trip -> tripMap[trip._id] = trip }
                triggerRefreshTrips()
            }, ::printError)
            .also { refreshTripsDisposable = it }
    }

    override fun createTrip(
        startPoint: Location,
        endPoint: Location,
        date: DetailedDate?,
    ): Single<Trip> {
        return tripRepo.createTrip(startPoint, endPoint, date)
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                tripMap[it._id] = it
                triggerRefreshTrips()
            }
    }

    override fun rejectParcel(parcelId: String, rejectData: ParcelRejectRequest): Completable {
        val trip = try {
            getTripByParcelId(parcelId)
        } catch (throwable: Throwable) {
            return Completable.error(throwable)
        }
        return parcelRepo.rejectParcel(parcelId, rejectData)
            .doOnSuccess {
                trip.parcelList.find { it._id == parcelId }?.status = ParcelStatus.REJECTED
                triggerRefreshTrips()
            }
            .flatMapCompletable {
                Completable.complete()
            }
    }

    override fun pickupParcel(parcelId: String): Completable {
        val trip = try {
            getTripByParcelId(parcelId)
        } catch (throwable: Throwable) {
            return Completable.error(throwable)
        }
        return parcelRepo.pickupParcel(parcelId)
            .doOnSuccess {
                trip.parcelList.find { it._id == parcelId }?.status = ParcelStatus.PICKED
                triggerRefreshTrips()
            }
            .flatMapCompletable {
                Completable.complete()
            }
    }

    override fun deliverParcel(parcelId: String, secret: String): Completable {
        val tripId = getTripByParcelId(parcelId)._id
        return parcelRepo.deliverParcel(parcelId, secret)
            .doOnSuccess {
                val trip = tripMap[tripId] ?: return@doOnSuccess
                trip.parcelList.find { it._id == parcelId }?.status = ParcelStatus.DELIVERED
                triggerRefreshTrips()
            }
            .flatMapCompletable {
                Completable.complete()
            }
    }

    override fun acceptParcel(parcelId: String): Completable {
        return parcelRepo.acceptParcel(parcelId)
            .doOnSuccess {
                val trip = tripMap.values.find { trip -> trip.status == TripStatus.ACTIVE }
                trip?.parcelList?.add(0, it)
                triggerRefreshTrips()
            }
            .flatMapCompletable {
                Completable.complete()
            }
    }

    override fun declineParcel(parcelId: String): Completable {
        return parcelRepo.declineParcel(parcelId)
            .flatMapCompletable {
                Completable.complete()
            }
    }

    private fun getTripByParcelId(parcelId: String): Trip {
        return tripMap.values.find {
            it.parcelList.map { parcel -> parcel._id }.contains(parcelId)
        } ?: error("No such trip with this parcel")
    }

    private fun triggerRefreshTrips() {
        tripListSubject.onNext(tripMap.values.toList())
    }

    override fun setAvailableParcel(parcel: Parcel) {
        availableParcelSubject.onNext(parcel)
    }

    override fun getAvailableParcelObservable(): Observable<Parcel> =
        availableParcelSubject

    override fun getAvailableParcelCancelledObservable(): Observable<Parcel> =
        availableParcelCancelledSubject

    override fun getTripListObservable(): Observable<List<Trip>> =
        tripListSubject
}