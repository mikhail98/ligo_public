package com.ligo.data.coordinator.trips

import com.ligo.core.Initializable
import com.ligo.data.model.DetailedDate
import com.ligo.data.model.Location
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRejectRequest
import com.ligo.data.model.Trip
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface IDriverTripCoordinator : Initializable {

    fun createTrip(startPoint: Location, endPoint: Location, date: DetailedDate?): Single<Trip>

    fun startTrip(tripId: String): Single<String>

    fun finishTrip(tripId: String): Single<String>

    fun rejectParcel(parcelId: String, rejectData: ParcelRejectRequest): Completable

    fun deliverParcel(parcelId: String, secret: String): Completable

    fun pickupParcel(parcelId: String): Completable

    fun acceptParcel(parcelId: String): Completable

    fun declineParcel(parcelId: String): Completable

    fun refreshTrips()

    fun setAvailableParcel(parcel: Parcel)

    fun getAvailableParcelObservable(): Observable<Parcel>

    fun getAvailableParcelCancelledObservable(): Observable<Parcel>

    fun getTripListObservable(): Observable<List<Trip>>
}