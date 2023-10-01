package com.ligo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationUpdate(

    @SerializedName("userId")
    val userId: String,

    @SerializedName("location")
    val location: Location,
) : Parcelable