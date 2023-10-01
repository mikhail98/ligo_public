package com.ligo.google.api

import com.google.gson.annotations.SerializedName

data class RemoteConfigParcelTypes(
    @SerializedName("types")
    val typeList: List<RemoteConfigParcelType>,
)

data class RemoteConfigParcelType(
    @SerializedName("type")
    val type: String,

    @SerializedName("titleKey")
    val titleKey: String,

    @SerializedName("descriptionKey")
    val descriptionKey: String,
)
