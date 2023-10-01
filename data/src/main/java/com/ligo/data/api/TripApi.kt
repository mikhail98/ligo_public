package com.ligo.data.api

import com.ligo.data.model.Trip
import com.ligo.data.model.TripRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TripApi {

    @POST("trips")
    fun createTrip(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Body request: TripRequest,
    ): Single<Trip>

    @GET("trips/{tripId}")
    fun getTripById(
        @Path("tripId") tripId: String,
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
    ): Single<Trip>

    @POST("trips/{tripId}/start")
    fun startTrip(
        @Path("tripId") tripId: String,
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
    ): Completable

    @POST("trips/{tripId}/finish")
    fun finishTrip(
        @Path("tripId") tripId: String,
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
    ): Completable
}