package com.ligo.data.coordinator.parcels

import com.ligo.core.Initializable
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface ISenderParcelsCoordinator : Initializable {

    fun createParcel(parcel: ParcelRequest): Single<Parcel>

    fun cancelParcel(parcelId: String): Completable

    fun refreshParcels()

    fun getParcelListObservable(): Observable<List<Parcel>>

    fun getOnParcelDeliveredObservable(): Observable<Parcel>
}