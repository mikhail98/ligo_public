package com.ligo.data.api

import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelRejectRequest
import com.ligo.data.model.ParcelRequest
import com.ligo.data.model.Secret
import com.ligo.data.model.SecretPayload
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ParcelApi {

    @POST("parcels")
    fun createParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Body parcel: ParcelRequest,
    ): Single<Parcel>

    @GET("parcels/{parcelId}")
    fun getParcelById(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
    ): Single<Parcel>

    @POST("parcels/{parcelId}/accept")
    fun acceptParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
    ): Single<Parcel>

    @POST("parcels/{parcelId}/decline")
    fun declineParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
    ): Single<Parcel>

    @POST("parcels/{parcelId}/pickup")
    fun pickupParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String
    ): Single<Parcel>

    @POST("parcels/{parcelId}/reject")
    fun rejectParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
        @Body rejectRequest: ParcelRejectRequest,
    ): Single<Parcel>

    @POST("parcels/{parcelId}/cancel")
    fun cancelParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
    ): Single<Parcel>

    @POST("parcels/{parcelId}/deliver")
    fun deliverParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
        @Body secret: SecretPayload,
    ): Single<Parcel>

    @POST("parcels/{parcelId}/secret")
    fun createSecret(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
        @Body secret: SecretPayload,
    ): Single<Secret>

    @GET("parcels/{parcelId}/secret")
    fun getSecret(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String,
    ): Single<Secret>
}