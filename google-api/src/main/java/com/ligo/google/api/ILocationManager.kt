package com.ligo.google.api

import android.location.Location
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Optional
import com.ligo.data.model.Location as LocationModel

interface ILocationManager {

    fun fetchLocationInfo(latitude: Double, longitude: Double): Single<Optional<LocationModel>>

    fun getLastKnownLocation(): Location?

    fun fetchLastKnownLocation(listener: (Location) -> Unit = {})

    fun getLocationUpdatesObservable(): Observable<Location>

    fun requestLocationUpdates()

    fun cancelLocationUpdates()
}