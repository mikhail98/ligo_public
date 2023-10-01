package com.ligo.data.api

import com.ligo.data.model.GMDirectionsResult
import com.ligo.data.model.GMSearchResult
import com.ligo.data.model.LocalizedConfig
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GoogleApi {

    @GET("google/places")
    fun searchForResults(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Query("query") query: String,
    ): Single<GMSearchResult>

    @GET("google/directions")
    fun searchForDirection(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
    ): Single<GMDirectionsResult>

    @GET("google/parseLocalization")
    fun parseLocalization(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
    ): Completable

    @GET("/google/localization")
    fun fetchLocalization(): Single<List<LocalizedConfig>>
}