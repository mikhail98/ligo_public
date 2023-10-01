package com.ligo.data.model

import com.google.gson.annotations.SerializedName

data class MessagesWereReadPayload(
    @SerializedName("chatId")
    val chatId: String
)
