package com.ligo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ligo.data.api.typeadapters.DefaultValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class Parcel(
    @SerializedName("_id") val _id: String,

    @SerializedName("sender") val sender: User,

    @SerializedName("driver") val driver: User?,

    @SerializedName("startPoint") val startPoint: Location,

    @SerializedName("endPoint") val endPoint: Location,

    @SerializedName("types") val types: List<String>,

    @SerializedName("notifiedDrivers") val notifiedDrivers: List<String> = listOf(),

    @SerializedName("driversBlacklist") val driversBlacklist: List<String> = listOf(),

    @SerializedName("status") var status: ParcelStatus,

    @SerializedName("price") val price: Price,

    @SerializedName("weight") val weight: Int?,

    @SerializedName("parcelPhoto") val parcelPhoto: String?,

    @SerializedName("rejectReason") val rejectReason: ParcelRejectReason?,

    @SerializedName("rejectComment") val rejectComment: String?,

    @SerializedName("rejectPhotoUrl") val rejectPickupPhotoUrl: String?,

    @SerializedName("createdAt") val createdAt: String,
) : Parcelable

data class ParcelRequest(
    @SerializedName("userId") val userId: String,

    @SerializedName("startPoint") val startPoint: Location,

    @SerializedName("endPoint") val endPoint: Location,

    @SerializedName("types") val types: List<String>,

    @SerializedName("price") val price: Price,

    @SerializedName("weight") val weight: Int,

    @SerializedName("secret") val secret: String,

    @SerializedName("parcelPhoto") val parcelPhoto: String,
)

@Parcelize
data class ParcelRejectRequest(
    @SerializedName("rejectReason") val rejectReason: ParcelRejectReason?,

    @SerializedName("rejectComment") val rejectComment: String?,

    @SerializedName("rejectPhotoUrl") val rejectPickupPhotoUrl: String?,
) : Parcelable

enum class ParcelStatus {

    @SerializedName("CREATED")
    CREATED,

    @SerializedName("ACCEPTED")
    ACCEPTED,

    @SerializedName("PICKED")
    PICKED,

    @SerializedName("DELIVERED")
    DELIVERED,

    @SerializedName("REJECTED")
    REJECTED,

    @SerializedName("CANCELLED")
    CANCELLED,

    @DefaultValue
    UNKNOWN
}

enum class ParcelRejectReason {

    @SerializedName("PARCEL_TOO_BIG")
    PARCEL_TOO_BIG,

    @SerializedName("CANT_FIND_SENDER")
    CANT_FIND_SENDER,

    @SerializedName("PARCEL_ILLEGAL")
    PARCEL_ILLEGAL,

    @DefaultValue
    @SerializedName("OTHER")
    OTHER;

    fun getConfigStringKey(): String? {
        return when (this) {
            CANT_FIND_SENDER -> ConfigStringKey.REJECT_PARCEL_REASON_CANT_FIND_SENDER
            PARCEL_ILLEGAL -> ConfigStringKey.REJECT_PARCEL_REASON_PARCEL_ILLEGAL
            PARCEL_TOO_BIG -> ConfigStringKey.REJECT_PARCEL_REASON_PARCEL_TOO_BIG
            else -> null
        }
    }
}

enum class DefaultParcelType {

    @SerializedName("SMALL")
    SMALL,

    @SerializedName("MEDIUM")
    MEDIUM,

    @SerializedName("LARGE")
    LARGE,

    @SerializedName("DOCUMENTS")
    DOCUMENTS,

    @SerializedName("OVERSIZE")
    OVERSIZE,

    @DefaultValue
    OTHER;

    companion object {
        fun stringify(typeList: List<String>): String {
            return typeList.joinToString(", ") { it.first().toString() }
        }

        fun fromCode(code: String): DefaultParcelType {
            return when (code) {
                "SMALL" -> SMALL
                "MEDIUM" -> MEDIUM
                "LARGE" -> LARGE
                "OVERSIZE" -> OVERSIZE
                "DOCUMENTS" -> DOCUMENTS
                else -> OTHER
            }
        }
    }
}