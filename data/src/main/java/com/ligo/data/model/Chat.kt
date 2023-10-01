package com.ligo.data.model

import com.google.gson.annotations.SerializedName
import com.ligo.data.api.typeadapters.DefaultValue

data class Chat(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("driver")
    val driver: User,
    @SerializedName("sender")
    val sender: User,
    @SerializedName("parcel")
    val parcel: Parcel,
    @SerializedName("messages")
    val messages: MutableList<Message>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    var updatedAt: String,
)

data class Message(
    @SerializedName("_id")
    val id: String,
    @SerializedName("user")
    val userId: String,
    @SerializedName("chat")
    val chatId: String,
    @SerializedName("text")
    val text: String?,
    @SerializedName("attachment")
    val attachment: Attachment?,
    @SerializedName("isDeleted")
    val isDeleted: Boolean,
    @SerializedName("isEdited")
    val isEdited: Boolean,
    @SerializedName("isRead")
    var isRead: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
)

data class Attachment(
    @SerializedName("type")
    val type: Type,

    @SerializedName("mediaUrl")
    var mediaUrl: String? = null,

    @SerializedName("location")
    val location: Location? = null,
) {
    enum class Type {
        @SerializedName("PHOTO")
        PHOTO,

        @SerializedName("VIDEO")
        VIDEO,

        @SerializedName("AUDIO")
        AUDIO,

        @SerializedName("LOCATION")
        LOCATION,

        @DefaultValue
        UNKNOWN
    }
}