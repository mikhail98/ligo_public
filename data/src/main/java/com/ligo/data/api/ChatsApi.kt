package com.ligo.data.api

import com.ligo.data.model.Chat
import com.ligo.data.model.CreateMessageRequest
import com.ligo.data.model.Message
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatsApi {

    @GET("chats")
    fun getChats(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
    ): Single<List<Chat>>

    @GET("chats/{chatId}")
    fun getChatById(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("chatId") chatId: String
    ): Single<Chat>

    @GET("chats/byParcel/{parcelId}")
    fun getChatByParcel(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("parcelId") parcelId: String
    ): Single<Chat>

    @POST("chats/{chatId}/sendMessage")
    fun sendMessage(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("chatId") chatId: String,
        @Body message: CreateMessageRequest
    ): Single<Message>

    @GET("chats/{chatId}/readMessages")
    fun readMessages(
        @Header(RetrofitBuilder.AUTH_TOKEN_PARAMETER) authToken: String,
        @Path("chatId") chatId: String,
    ): Completable
}