package com.ligo.data.repo.trip

import com.ligo.data.model.DetailedDate
import com.ligo.data.model.Location
import com.ligo.data.model.Trip
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface ITripRepo {

    fun createTrip(
        startPoint: Location,
        endPoint: Location,
        date: DetailedDate?,
    ): Single<Trip>

    fun getTripById(tripId: String): Single<Trip>

    fun startTrip(tripId: String): Completable

    fun finishTrip(tripId: String): Completable
}