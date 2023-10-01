package com.ligo.data.api

import com.ligo.data.model.FCMToken
import com.ligo.data.model.LoginUser
import com.ligo.data.model.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @POST("/auth/login")
    fun login(@Body request: LoginUser): Single<User>

    @POST("/auth/{userId}/logout")
    fun logout(
        @Path("userId") userId: String,
        @Body fcmToken: FCMToken,
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
    ): Completable

    @POST("/auth/{userId}/delete")
    fun deleteAccount(
        @Path("userId") userId: String,
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
    ): Completable
}