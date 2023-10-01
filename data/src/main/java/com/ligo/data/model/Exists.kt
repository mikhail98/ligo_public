package com.ligo.data.model

import com.google.gson.annotations.SerializedName

class Exists(
    @SerializedName("userExists")
    val userExists: Boolean,
)