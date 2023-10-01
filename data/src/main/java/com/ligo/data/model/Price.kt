package com.ligo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Price(
    @SerializedName("value")
    val value: Int,
    @SerializedName("currency")
    val currency: String,
) : Parcelable