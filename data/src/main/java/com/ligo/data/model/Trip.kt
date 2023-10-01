package com.ligo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ligo.data.api.typeadapters.DefaultValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trip(
    @SerializedName("_id")
    val _id: String,

    @SerializedName("driver")
    val driver: User,

    @SerializedName("startPoint")
    val startPoint: Location,

    @SerializedName("endPoint")
    val endPoint: Location,

    @SerializedName("parcels")
    val parcelList: MutableList<Parcel> = mutableListOf(),

    @SerializedName("status")
    var status: TripStatus,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("date")
    val date: DetailedDate?,
) : Parcelable

enum class TripStatus {

    @SerializedName("SCHEDULED")
    SCHEDULED,

    @SerializedName("ACTIVE")
    ACTIVE,

    @SerializedName("FINISHED")
    FINISHED,

    @DefaultValue
    UNKNOWN
}

data class TripRequest(
    @SerializedName("startPoint")
    val startPoint: Location,

    @SerializedName("endPoint")
    val endPoint: Location,

    @SerializedName("date")
    val date: DetailedDate?,
)

@Parcelize
data class DetailedDate(
    @SerializedName("second")
    val second: Int,

    @SerializedName("minute")
    val minute: Int,

    @SerializedName("hour")
    val hour: Int,

    @SerializedName("day")
    val day: Int,

    @SerializedName("month")
    val month: Int,
) : Parcelable