package com.ligo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ligo.data.api.typeadapters.DefaultValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("_id")
    val _id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: UserRole,

    @SerializedName("location")
    val location: Location?,

    @SerializedName("avatarPhoto")
    val avatarPhoto: String?,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("ratings")
    val ratings: List<Rating>,

    @SerializedName("token")
    val authToken: String?,
) : Parcelable

enum class UserRole {
    @SerializedName("DRIVER")
    DRIVER,

    @DefaultValue
    @SerializedName("SENDER")
    SENDER
}

data class UserRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("googleToken")
    val googleToken: String? = null,

    @SerializedName("role")
    val role: UserRole? = null,

    @SerializedName("fcmToken")
    val fcmToken: String? = null,

    @SerializedName("phone")
    val phone: String? = null,
)

data class LoginUser(
    @SerializedName("email")
    val email: String,

    @SerializedName("googleToken")
    val googleToken: String,
)

data class Email(
    @SerializedName("email")
    val email: String,
)

@Parcelize
data class Rating(
    @SerializedName("userFrom")
    val userId: String,

    @SerializedName("rating")
    val rating: Int,
) : Parcelable
