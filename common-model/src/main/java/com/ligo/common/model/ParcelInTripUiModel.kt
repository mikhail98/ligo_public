package com.ligo.common.model

import com.ligo.core.getTimeInMillis
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelStatus

data class ParcelInTripUiModel(
    val parcelId: String,
    val createdAt: Long,
    val cityName: String,
    val address: String,
    val parcelPhoto: String?,
    val status: StatusUiModel,
    val unreadMessageCount: Int,
    val priority: Int,
) {

    companion object {

        fun fromParcel(unreadMessageCount: Int, parcel: Parcel): ParcelInTripUiModel {
            val endPoint = if (parcel.endPoint.cityName != null) {
                parcel.endPoint.cityName + " (" + parcel.endPoint.fullName + ")"
            } else {
                parcel.endPoint.fullName
            }

            val priority = when (parcel.status) {
                ParcelStatus.ACCEPTED -> 4
                ParcelStatus.PICKED -> 3
                ParcelStatus.DELIVERED -> 2
                ParcelStatus.REJECTED -> 1
                else -> 0
            }

            return ParcelInTripUiModel(
                priority = priority,
                parcelId = parcel._id,
                cityName = endPoint.orEmpty(),
                createdAt = getTimeInMillis(parcel.createdAt),
                parcelPhoto = parcel.parcelPhoto,
                address = parcel.endPoint.address.orEmpty(),
                status = StatusUiModel.fromParcel(parcel, false),
                unreadMessageCount = unreadMessageCount
            )
        }
    }
}
