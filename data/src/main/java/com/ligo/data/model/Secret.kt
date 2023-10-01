package com.ligo.data.model

import com.google.gson.annotations.SerializedName

data class Secret(
    @SerializedName("userId") val userId: String,
    @SerializedName("parcelId") val parcelId: String,
    @SerializedName("secret") val secret: String,
)

data class SecretPayload(
    @SerializedName("secret") val secret: String,
)