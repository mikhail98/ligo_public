package com.ligo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GMSearchResult(
    @SerializedName("results") val results: List<GMLocationResult>,
) : Parcelable

@Parcelize
data class GMLocationResult(
    @SerializedName("formatted_address") val address: String,

    @SerializedName("geometry") val geometry: GMGeometry,

    @SerializedName("name") val name: String,

    @SerializedName("icon") val iconUrl: String?,
) : Parcelable

@Parcelize
data class GMGeometry(
    @SerializedName("location") val location: GMPoint,
) : Parcelable

@Parcelize
data class GMPoint(
    @SerializedName("lat") val lat: Double,

    @SerializedName("lng") val lng: Double,
) : Parcelable

@Parcelize
data class GMDirectionsResult(
    @SerializedName("points") val points: String,
    @SerializedName("distance") val distance: Float,
    @SerializedName("duration") val duration: Long,
) : Parcelable