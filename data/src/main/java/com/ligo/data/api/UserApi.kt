package com.ligo.data.api

import com.ligo.data.api.RetrofitBuilder.Companion.AUTH_TOKEN_PARAMETER
import com.ligo.data.model.AvatarPhoto
import com.ligo.data.model.Email
import com.ligo.data.model.Exists
import com.ligo.data.model.FCMToken
import com.ligo.data.model.LocationRequest
import com.ligo.data.model.Parcel
import com.ligo.data.model.PassportPhoto
import com.ligo.data.model.Trip
import com.ligo.data.model.User
import com.ligo.data.model.UserRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @POST("users/")
    fun createUser(
        @Body request: UserRequest,
    ): Single<User>

    @GET("users/{userId}")
    fun getUserById(
        @Path("userId") userId: String,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
    ): Single<User>

    @GET("users/{userId}/driverTrips")
    fun getDriverTrips(
        @Path("userId") userId: String,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
    ): Single<List<Trip>>

    @GET("users/{userId}/senderParcels")
    fun getSenderParcels(
        @Path("userId") userId: String,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
    ): Single<List<Parcel>>

    @PATCH("users/{userId}/fcmToken")
    fun updateFcmToken(
        @Path("userId") userId: String,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
        @Body request: FCMToken,
    ): Completable

    @PATCH("users/{userId}/location")
    fun updateLocation(
        @Path("userId") userId: String,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
        @Body request: LocationRequest,
    ): Completable

    @PATCH("users/{userId}/passportPhoto")
    fun updatePassportPhotoUrl(
        @Path("userId") userId: String,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
        @Body request: PassportPhoto,
    ): Completable

    @PATCH("users/{userId}/avatarPhoto")
    fun updateAvatarPhoto(
        @Path("userId") userId: String,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
        @Body request: AvatarPhoto,
    ): Completable

    @PATCH("users/{userId}/rating/{rating}")
    fun updateUserRating(
        @Path("userId") userId: String,
        @Path("rating") rating: Int,
        @Header(AUTH_TOKEN_PARAMETER) authToken: String,
    ): Completable

    @POST("users/exists")
    fun checkUserExists(
        @Body email: Email,
    ): Single<Exists>
}