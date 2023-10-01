package com.ligo.data.model

import com.google.gson.annotations.SerializedName

data class PassportPhoto(
    @SerializedName("passportPhoto")
    val passportPhoto: String
)