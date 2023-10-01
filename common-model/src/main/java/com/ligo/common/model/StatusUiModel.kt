package com.ligo.common.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.ligo.core.R
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.Parcel
import com.ligo.data.model.ParcelStatus
import com.ligo.data.model.Trip
import com.ligo.data.model.TripStatus

sealed class StatusUiModel(
    @DrawableRes val mainIconRes: Int,
    val configStringKey: String,
    @ColorRes val textColorRes: Int,
    @ColorRes val bgColorRes: Int,
    @DrawableRes val iconRes: Int,
) {

    companion object {
        fun fromParcel(parcel: Parcel, isUserParcel: Boolean): StatusUiModel {
            return when (parcel.status) {
                ParcelStatus.CREATED -> ParcelCreated
                ParcelStatus.ACCEPTED -> if (isUserParcel) ParcelAccepted else ParcelNotPicked
                ParcelStatus.PICKED -> ParcelPicked
                ParcelStatus.DELIVERED -> ParcelDelivered
                ParcelStatus.REJECTED -> ParcelRejected
                ParcelStatus.CANCELLED -> ParcelCanceled
                else -> ParcelCanceled
            }
        }

        fun fromTrip(trip: Trip): StatusUiModel {
            return when (trip.status) {
                TripStatus.ACTIVE -> TripActive
                TripStatus.FINISHED -> TripFinished
                TripStatus.SCHEDULED -> TripScheduled
                else -> TripFinished
            }
        }
    }

    object TripActive : StatusUiModel(
        mainIconRes = R.drawable.ic_car_dark,
        configStringKey = ConfigStringKey.HISTORY_TRIP_STATUS_ACTIVE,
        textColorRes = R.color.white,
        bgColorRes = R.color.accent,
        iconRes = R.drawable.ic_telegram,
    )

    object TripFinished : StatusUiModel(
        mainIconRes = R.drawable.ic_car,
        configStringKey = ConfigStringKey.HISTORY_TRIP_STATUS_FINISHED,
        textColorRes = R.color.blue_super_light,
        bgColorRes = R.color.semi_transparent_blue,
        iconRes = R.drawable.ic_delivered,
    )

    object TripScheduled : StatusUiModel(
        mainIconRes = R.drawable.ic_car,
        configStringKey = ConfigStringKey.HISTORY_TRIP_STATUS_SCHEDULED,
        textColorRes = R.color.white,
        bgColorRes = R.color.semi_transparent_blue,
        iconRes = R.drawable.ic_telegram,
    )

    object ParcelCreated : StatusUiModel(
        mainIconRes = R.drawable.ic_package,
        configStringKey = ConfigStringKey.HISTORY_PARCEL_STATUS_NO_DRIVER,
        textColorRes = R.color.white,
        bgColorRes = R.color.semi_transparent_gray,
        iconRes = R.drawable.ic_clock,
    )

    object ParcelAccepted : StatusUiModel(
        mainIconRes = R.drawable.ic_package,
        configStringKey = ConfigStringKey.HISTORY_PARCEL_STATUS_WAITING_FOR_DRIVER,
        textColorRes = R.color.white,
        bgColorRes = R.color.semi_transparent_blue,
        iconRes = R.drawable.ic_car,
    )

    object ParcelNotPicked : StatusUiModel(
        mainIconRes = R.drawable.ic_package,
        configStringKey = ConfigStringKey.HISTORY_PARCEL_STATUS_NOT_PICKED,
        textColorRes = R.color.red_text,
        bgColorRes = R.color.semi_transparent_red,
        iconRes = R.drawable.ic_not_picked
    )

    object ParcelPicked : StatusUiModel(
        mainIconRes = R.drawable.ic_package,
        configStringKey = ConfigStringKey.HISTORY_PARCEL_STATUS_ON_BOARD,
        textColorRes = R.color.white,
        bgColorRes = R.color.semi_transparent_gray,
        iconRes = R.drawable.ic_on_board,
    )

    object ParcelDelivered : StatusUiModel(
        mainIconRes = R.drawable.ic_package,
        configStringKey = ConfigStringKey.HISTORY_PARCEL_STATUS_DELIVERED,
        textColorRes = R.color.blue_super_light,
        bgColorRes = R.color.semi_transparent_blue,
        iconRes = R.drawable.ic_delivered,
    )

    object ParcelRejected : StatusUiModel(
        mainIconRes = R.drawable.ic_package,
        configStringKey = ConfigStringKey.HISTORY_PARCEL_STATUS_REJECTED,
        textColorRes = R.color.red_text,
        bgColorRes = R.color.semi_transparent_red,
        iconRes = R.drawable.ic_remove_circle,
    )

    object ParcelCanceled : StatusUiModel(
        mainIconRes = R.drawable.ic_package,
        configStringKey = ConfigStringKey.HISTORY_PARCEL_STATUS_CANCELED,
        textColorRes = R.color.red_text,
        bgColorRes = R.color.semi_transparent_red,
        iconRes = R.drawable.ic_remove_circle,
    )
}