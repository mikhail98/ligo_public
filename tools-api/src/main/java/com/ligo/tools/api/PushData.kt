package com.ligo.tools.api

import com.google.gson.annotations.SerializedName
import com.ligo.data.model.Message

data class PushData(
    @SerializedName("key")
    val key: String,

    @SerializedName("parcelId")
    val parcelId: String?,

    @SerializedName("senderId")
    val senderId: String?,

    @SerializedName("tripId")
    val tripId: String?,

    @SerializedName("route")
    val route: String?,

    @SerializedName("message")
    val message: Message?,

    @SerializedName("chat")
    val chat: Chat?,
) {
    data class Chat(
        @SerializedName("_id")
        val id: String,

        @SerializedName("driverId")
        val driverId: String,

        @SerializedName("senderName")
        val senderName: String,

        @SerializedName("senderAvatar")
        val senderAvatar: String?,

        @SerializedName("driverName")
        val driverName: String,

        @SerializedName("driverAvatar")
        val driverAvatar: String?,
    )
}