package com.ligo.feature.parcelintrip

class ParcelScanData(
    val type: Type,
    val parcelId: String,
    val secret: String?,
) {
    enum class Type {
        PICKUP, DELIVERY
    }
}