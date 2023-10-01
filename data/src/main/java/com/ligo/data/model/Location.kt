package com.ligo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("cityName")
    val cityName: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("name")
    val fullName: String? = null,
) : Parcelable

@Parcelize
data class LocationRequest(
    @SerializedName("location")
    val location: Location,
) : Parcelable