package com.ligo.data.repo.trip

import com.ligo.data.api.TripApi
import com.ligo.data.model.DetailedDate
import com.ligo.data.model.Location
import com.ligo.data.model.Trip
import com.ligo.data.model.TripRequest
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.BaseRepo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class TripRepo(
    private val appPreferences: IAppPreferences,
    private val tripApi: TripApi,
) : BaseRepo(), ITripRepo {

    private fun getAuthToken(): String {
        return appPreferences.getUser()?.authToken.orEmpty()
    }

    override fun createTrip(
        startPoint: Location,
        endPoint: Location,
        date: DetailedDate?,
    ): Single<Trip> = tripApi.createTrip(
        getAuthToken(),
        TripRequest(startPoint, endPoint, date)
    ).proceedWithApiThrowable()

    override fun getTripById(tripId: String): Single<Trip> = tripApi.getTripById(
        tripId,
        getAuthToken()
    ).proceedWithApiThrowable()

    override fun startTrip(tripId: String): Completable = tripApi.startTrip(
        tripId,
        getAuthToken()
    ).proceedWithApiThrowable()

    override fun finishTrip(tripId: String): Completable = tripApi.finishTrip(
        tripId,
        getAuthToken()
    ).proceedWithApiThrowable()
}
