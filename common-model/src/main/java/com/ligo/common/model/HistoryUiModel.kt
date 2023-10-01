package com.ligo.common.model

import com.ligo.data.model.Parcel
import com.ligo.data.model.Trip

data class HistoryUiModel(
    val id: String,
    val trip: Trip?,
    val parcel: Parcel?,
    val createdAt: String,
    val routeText: String,
    val uiStatus: StatusUiModel,
) {
    companion object {
        fun fromTrip(trip: Trip): HistoryUiModel {
            val uiStatus = StatusUiModel.fromTrip(trip)
            val createdAt = trip.createdAt
            val routeText = "${trip.startPoint.cityName} - ${trip.endPoint.cityName}"
            return HistoryUiModel(trip._id, trip, null, createdAt, routeText, uiStatus)
        }

        fun fromParcel(parcel: Parcel): HistoryUiModel {
            val uiStatus = StatusUiModel.fromParcel(parcel, true)
            val createdAt = parcel.createdAt
            val routeText = "${parcel.startPoint.cityName} - ${parcel.endPoint.cityName}"
            return HistoryUiModel(parcel._id, null, parcel, createdAt, routeText, uiStatus)
        }
    }
}