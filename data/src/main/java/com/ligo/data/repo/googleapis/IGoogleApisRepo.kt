package com.ligo.data.repo.googleapis

import com.ligo.data.model.GMDirectionsResult
import com.ligo.data.model.GMSearchResult
import com.ligo.data.model.LocalizedConfig
import com.ligo.data.model.Location
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IGoogleApisRepo {

    fun searchForResults(query: String): Single<GMSearchResult>

    fun searchForDirection(origin: Location, destination: Location): Single<GMDirectionsResult>

    fun fetchLocalization(): Single<List<LocalizedConfig>>

    fun parseLocalization(): Completable
}