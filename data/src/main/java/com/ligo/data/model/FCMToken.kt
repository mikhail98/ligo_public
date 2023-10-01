package com.ligo.data.model

import com.google.gson.annotations.SerializedName

data class FCMToken(
    @SerializedName("fcmToken")
    val fcmToken: String?
)