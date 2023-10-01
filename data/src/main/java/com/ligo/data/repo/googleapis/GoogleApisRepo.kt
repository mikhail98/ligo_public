package com.ligo.data.repo.googleapis

import com.ligo.data.api.GoogleApi
import com.ligo.data.model.GMDirectionsResult
import com.ligo.data.model.GMSearchResult
import com.ligo.data.model.LocalizedConfig
import com.ligo.data.model.Location
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.BaseRepo
import com.ligo.data.room.RouteDao
import com.ligo.data.room.RouteEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal class GoogleApisRepo(
    private val appPreferences: IAppPreferences,
    private val googleApi: GoogleApi,
    private val routeDao: RouteDao,
) : BaseRepo(), IGoogleApisRepo {

    private fun getAuthToken(): String {
        return appPreferences.getUser()?.authToken.orEmpty()
    }

    override fun searchForResults(query: String): Single<GMSearchResult> {
        return googleApi.searchForResults(getAuthToken(), query)
            .proceedWithApiThrowable()
    }

    override fun searchForDirection(
        origin: Location,
        destination: Location,
    ): Single<GMDirectionsResult> {
        return routeDao.getRouteByLocationObservable(
            origin.latitude,
            origin.longitude,
            destination.latitude,
            destination.longitude
        )
            .map { GMDirectionsResult(it.points, it.distance, it.duration) }
            .onErrorResumeNext {
                val start = "${origin.latitude},${origin.longitude}"
                val end = "${destination.latitude},${destination.longitude}"
                googleApi.searchForDirection(getAuthToken(), start, end)
                    .doOnSuccess {
                        routeDao.insertRoute(
                            RouteEntity(
                                origin.latitude,
                                origin.longitude,
                                destination.latitude,
                                destination.longitude,
                                it.duration,
                                it.distance,
                                it.points
                            )
                        )
                    }
                    .proceedWithApiThrowable()
            }
    }

    override fun fetchLocalization(): Single<List<LocalizedConfig>> {
        return googleApi.fetchLocalization()
            .proceedWithApiThrowable()
    }

    override fun parseLocalization(): Completable {
        return googleApi.parseLocalization(getAuthToken())
            .proceedWithApiThrowable()
    }
}