package com.ligo.feature.rejectparcel

import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ParcelRejectReason

class RejectReasonUi(
    val textConfigStringKey: String,
    val reason: ParcelRejectReason,
    var isChecked: Boolean,
) {
    companion object {
        fun getReasons(): List<RejectReasonUi> {
            return listOf(
                RejectReasonUi(
                    ConfigStringKey.REJECT_PARCEL_REASON_PARCEL_TOO_BIG,
                    ParcelRejectReason.PARCEL_TOO_BIG,
                    false
                ),
                RejectReasonUi(
                    ConfigStringKey.REJECT_PARCEL_REASON_PARCEL_ILLEGAL,
                    ParcelRejectReason.PARCEL_ILLEGAL,
                    false
                ),
                RejectReasonUi(
                    ConfigStringKey.REJECT_PARCEL_REASON_ANOTHER,
                    ParcelRejectReason.OTHER,
                    false
                )
            )
        }
    }
}